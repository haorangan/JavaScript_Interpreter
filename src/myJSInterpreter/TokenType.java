package myJSInterpreter;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, MOD, TERNARY, COLON,
    LEFT_BRACKET, RIGHT_BRACKET,
    // One or two character tokens.
    EMARK, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    PLUSPLUS, MINUSMINUS,
    PLUSEQUAL, MINUSEQUAL,
    MULTIPLYEQUAL, DIVIDEEQUAL,
    MODEQUAL,
    // Literals.
    IDENTIFIER, STRING, NUMBER,
    // Keywords.
    AND, CLASS, ELSE, FALSE, FUNCTION, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    EOF
}