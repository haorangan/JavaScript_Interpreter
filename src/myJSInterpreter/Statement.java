package myJSInterpreter;

import java.util.List;

public abstract class Statement {
    interface Visitor<R> {
        R visitReturnStmt(Return stmt);
        R visitFunctionStmt(Function stmt);
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
    }
    public static class Return extends Statement {
        Return(Token keyword, Expr value) {
           this.keyword = keyword;
           this.value = value;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }
        final Token keyword;
        final Expr value;
    }
    public static class Function extends Statement {
        Function(Token name, List<Token> params, List<Statement> body) {
           this.name = name;
           this.params = params;
           this.body = body;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitFunctionStmt(this);
    }
        final Token name;
        final List<Token> params;
        final List<Statement> body;
    }
    public static class Block extends Statement {
        Block(List<Statement> statements) {
           this.statements = statements;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }
        final List<Statement> statements;
    }
    public static class Class extends Statement {
        Class(Token name, List<Statement.Function> methods) {
           this.name = name;
           this.methods = methods;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitClassStmt(this);
    }
        final Token name;
        final List<Statement.Function> methods;
    }
    public static class Expression extends Statement {
        Expression(Expr expression) {
           this.expression = expression;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }
        final Expr expression;
    }
    public static class If extends Statement {
        If(Expr condition, Statement thenBranch, Statement elseBranch) {
           this.condition = condition;
           this.thenBranch = thenBranch;
           this.elseBranch = elseBranch;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }
        final Expr condition;
        final Statement thenBranch;
        final Statement elseBranch;
    }
    public static class Print extends Statement {
        Print(Expr expression) {
           this.expression = expression;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintStmt(this);
    }
        final Expr expression;
    }
    public static class Var extends Statement {
        Var(Token name, Expr initializer) {
           this.name = name;
           this.initializer = initializer;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitVarStmt(this);
    }
        final Token name;
        final Expr initializer;
    }
    public static class While extends Statement {
        While(Expr condition, Statement body) {
           this.condition = condition;
           this.body = body;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitWhileStmt(this);
    }
        final Expr condition;
        final Statement body;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
