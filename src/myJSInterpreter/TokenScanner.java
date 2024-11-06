package myJSInterpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static myJSInterpreter.TokenType.*;

public class TokenScanner {

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("function", FUNCTION);
        keywords.put("if", IF);
        keywords.put("null", NIL);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public TokenScanner(String source) {
        this.source = source;
    }
    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '%':
                addToken(match('=') ? MODEQUAL: MOD);
                break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ']': addToken(RIGHT_BRACKET); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-':
                char peek = peek();
                if(peek == '=') {
                    addToken(MINUSEQUAL);
                    advance();
                }
                else if(peek == '-') {
                    addToken(MINUSMINUS);
                    advance();
                }
                else {
                    addToken(MINUS);
                }
                break;
            case '+':
                char peek2 = peek();
                if(peek2 == '=') {
                    addToken(PLUSEQUAL);
                    advance();
                }
                else if(peek2 == '+') {
                    addToken(PLUSPLUS);
                    advance();
                }
                else {
                    addToken(PLUS);
                }
                break;
            case '*':
                addToken(match('=') ? MULTIPLYEQUAL : STAR);
                break;
            case ';': addToken(SEMICOLON); break;
            case '?': addToken(TERNARY); break;
            case ':': addToken(COLON); break;
            case '|':
                addToken(match('|') ? OR : OR);
                break;
            case '&':
                addToken(match('&') ? AND : AND);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : EMARK);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(match('=') ? DIVIDEEQUAL : SLASH);
                }
                break;
            default:
                if(isDigit(c)) {
                    number();
                }
                else if(isAlpha(c)) {
                    identifier();
                }
                else {
                    JavaScript.error(line, "Unexpected character '" + c + "'");
                }
                break;
        }
    }
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private void number() {
        while(isDigit(peek())) advance();
        if(peek() == '.' && isDigit(peekNext())) {
            advance();
            while(isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            JavaScript.error(line, "Unterminated string.");
            return;
        }
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);

    }
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }
    private boolean isAtEnd() {
        return current >= source.length();
    }
    private char advance() {
        return source.charAt(current++);
    }
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
