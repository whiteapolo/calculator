import java.util.ArrayList;

public class Lexer {
    String source;
    int start;
    int curr;
    int line;

    public static class SyntaxError extends Exception {}

    Lexer(final Lexer lexer) {
        this.source = lexer.source;
        this.start = lexer.start;
        this.curr = lexer.curr;
        this.line = 1;
    }

    Lexer(String source) {
        this.source = source;
        this.start = 0;
        this.curr = 0;
        this.line = 1;
    }

    private boolean isAtEnd() {
        return curr >= source.length();
    }

    Token peek(final Lexer lexer) {
        Lexer tmp = new Lexer(lexer);
        return tmp.next();
    }

    Token next() {
        start = curr;
        skipCset(" \t\n");

        if (isAtEnd())
            return new Token(TokenType.END, 0, "", line);

        final char c = source.charAt(curr);
        if (isAlpha(c))
            return identifierToken();
        if (Character.isDigit(c))
            return numberToken();

        return switch (source.charAt(curr)) {
            case '=' -> new Token(TokenType.EQUAL, '=', source.substring(start, ++curr), line);
            case '+' -> new Token(TokenType.PLUS, '+', source.substring(start, ++curr), line);
            case '-' -> new Token(TokenType.MINUS, '-', source.substring(start, ++curr), line);
            case '*' -> new Token(TokenType.MULTIPLY, '*', source.substring(start, ++curr), line);
            case '/' -> new Token(TokenType.DIVIDE, '/', source.substring(start, ++curr), line);
            case '^' -> new Token(TokenType.POWER, '^', source.substring(start, ++curr), line);
            case '(' -> new Token(TokenType.OPEN_PAREN, '(', source.substring(start, ++curr), line);
            case ')' -> new Token(TokenType.CLOSE_PAREN, ')', source.substring(start, ++curr), line);
            case '$' -> new Token(TokenType.LAST_RESULT, 0, source.substring(start, ++curr), line);
            default -> new Token(TokenType.ERROR, 0, source.substring(start, ++curr), line);
        };
    }

    ArrayList<Token> scanTokens() throws SyntaxError {
        ArrayList<Token> tokens = new ArrayList<>();

        Token t = next();
        for (; t.type() != TokenType.END && t.type() != TokenType.ERROR; t = next())
            tokens.add(t);

        if (t.type() == TokenType.ERROR)
            reportError(String.format("Unexpected '%s'", t.lexeme()));

        tokens.add(t);
        return tokens;
    }

    void reportError(final String msg) throws SyntaxError {
        System.out.println("Syntax Error: " + msg);
        throw new SyntaxError();
    }

    void printAllTokens() {
        Lexer tmpLexer = new Lexer(this);
        Token t = tmpLexer.next();
        while (t.type() != TokenType.END) {
            System.out.println(t);
            t = tmpLexer.next();
        }
    }

    private void skipCset(final String cset) {
        while (start < source.length() && cset.indexOf(source.charAt(start)) != -1) {
            if (source.charAt(start) == '\n')
                line++;
            start++;
        }
        curr = start;
    }

    private boolean isAlpha(final char c) {
        final char d = Character.toLowerCase(c);
        return ('a' <= d && d <= 'z') || d == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return Character.isDigit(c) || isAlpha(c);
    }

    private Token identifierToken() {
        curr++;
        while (!isAtEnd() && isAlphaNumeric(source.charAt(curr)))
            curr++;
        return new Token(TokenType.IDENTIFIER, 0, source.substring(start, curr), line);
    }

    private Token numberToken() {
        curr++;
        while (!isAtEnd() && Character.isDigit(source.charAt(curr)))
            curr++;

        if (!isAtEnd() && source.charAt(curr) == '.') {
            curr++;
            while (!isAtEnd() && Character.isDigit(source.charAt(curr)))
                curr++;
        }

        final String lexeme = source.substring(start, curr);
        return new Token(TokenType.NUMBER, Double.parseDouble(lexeme), lexeme, line);
    }
}