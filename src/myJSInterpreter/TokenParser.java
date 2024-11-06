package myJSInterpreter;

import java.util.*;
import static myJSInterpreter.TokenType.*;


public class TokenParser {
    private final List<Token> tokens;
    private static class ParseError extends RuntimeException {}
    private int current = 0;
    public TokenParser(List<Token> tokens) {
        this.tokens = tokens;
    }
    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
    private Statement declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }


    private Statement statement() {
        if(match(CLASS)) return classDeclaration();
        if (match(RETURN)) return returnStatement();
        if (match(FUNCTION)) return function("function");
        if (match(IF)) return ifStatement();
        if (match(FOR)) return forStatement();
        if(match(WHILE)) return whileStatement();
        if (match(PRINT)) return printStatement();
        if(match(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Statement.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Statement.Class(name, methods);
    }

    private Statement.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
    }
    private Statement returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }
    private Statement forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        }
        else if (match(VAR)) {
            initializer = varDeclaration();
        }
        else {
            initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");
        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Statement body = statement();
        if (increment != null) {
            body = new Statement.Block(Arrays.asList(body, new Statement.Expression(increment)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Statement.While(condition, body);
        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }
        return body;
    }
    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();
        return new Statement.While(condition, body);
    }
    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Statement.If(condition, thenBranch, elseBranch);
    }
    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    private Statement printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }
    private Statement expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(expr);
    }
    private Expr expression() {
        return assignment();
    }
    private Expr assignment() {
        Expr expr = ternary();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        else if (match(PLUSEQUAL) || match(MINUSEQUAL) || match(MULTIPLYEQUAL)
        || match(DIVIDEEQUAL) || match(MODEQUAL)) {
            Token operator = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.IncDec(name, operator, value);
            }
            error(operator, "Invalid assignment target.");
        }
        else if(match(PLUSPLUS) || match(MINUSMINUS)) {
            Token operator = previous();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.IncDec(name, operator, new Expr.Literal(1.0));
            }
            error(operator, "Invalid assignment target.");
        }
        return expr;
    }
    private Expr ternary() {
        Expr expr = logical();
        if(match(TERNARY)) {
            Expr left = expression();
            if(match(COLON)) {
                Expr right = expression();
                expr = new Expr.Ternary(expr, left, right);
            }
            else {
                throw error(peek(), "Expected ternary expression");
            }
        }
        return expr;
    }
    private Expr logical() {
        Expr expr = equality();
        while(match(AND, OR)) {
            Token operator = previous();
            Expr right = expression();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR, MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr unary() {
        if (match(EMARK, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }
    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            }
            else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            }
            else if(match(LEFT_BRACKET)) {
                Expr index = expression();
                consume(RIGHT_BRACKET, "Expect ]");
                expr = new Expr.ArrayGet(previous(), expr , index);
            }
            else {
                break;
            }
        }
        return expr;
    }
    private Expr primary() {
        if (match(THIS)) return new Expr.This(previous());
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(LEFT_BRACKET)) {
            List<Expr> exprs = new ArrayList<>();
            if(match(RIGHT_BRACKET)) {
                return new Expr.Array(exprs);
            }
            else {
                do {
                    exprs.add(expression());
                } while (match(COMMA));  // Allow comma between expressions
                consume(RIGHT_BRACKET, "Expect ']' after array literal.");
            }
            return new Expr.Array(exprs);
        }

        throw error(peek(), "Expect expression.");
    }
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
    private Token peek() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current - 1);
    }
    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }
    private ParseError error(Token token, String message) {
        JavaScript.error(token, message);
        return new ParseError();
    }
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS:
                case FUNCTION:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }
}
