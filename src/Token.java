enum TokenType {
    NUMBER,
    MINUS,
    PLUS,
    MULTIPLY,
    DIVIDE,
    POWER,
    OPEN_PAREN,
    CLOSE_PAREN,
    LAST_RESULT,
    IDENTIFIER,
    EQUAL,
    ERROR,
    END,
}

record Token(TokenType type, double value, String lexeme, int line) {}