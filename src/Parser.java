import java.util.List;

public class Parser {
    boolean hadError = false;
    List<Token> tokens;
    int curr = 0;

    public static class ParseError extends Exception {}

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr parseLiteral() {
        if (match(TokenType.NUMBER))
            return new Expr.Literal(previous());
        if (match(TokenType.IDENTIFIER, TokenType.LAST_RESULT))
            return new Expr.Identifier(previous());
        if (match(TokenType.OPEN_PAREN)) {
            Expr expr = parseExpression();
            if (!match(TokenType.CLOSE_PAREN))
                reportError("Expected ')' after expression.", peek().line());
            return expr;
        }
        reportError("Expected expression.", peek().line());
        return null;
    }

    private Expr parseUnary() {
        if (match(TokenType.MINUS)) {
            Token operator = previous();
            Expr right = parseUnary();
            if (right == null)
                reportError(String.format("Missing expression after unary: '%s'", operator.lexeme()), peek().line());
            return new Expr.Unary(operator, right);
        }
        return parseLiteral();
    }

    private Expr parsePower() {
        Expr expr = parseUnary();

        while (match(TokenType.POWER)) {
            Token operator = previous();

            if (expr == null)
                reportError(String.format("Missing expression before: '%s'", operator.lexeme()), peek().line());

            Expr right = parsePower();
            if (right == null)
                reportError(String.format("Missing expression after: '%s'", operator.lexeme()), peek().line());

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseFactor() {
        Expr expr = parsePower();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            Token operator = previous();

            if (expr == null)
                reportError(String.format("Missing expression before: '%s'", operator.lexeme()), peek().line());

            Expr right = parsePower();
            if (right == null)
                reportError(String.format("Missing expression after: '%s'", operator.lexeme()), peek().line());
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseTerm() {
        Expr expr = parseFactor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();

            if (expr == null)
                reportError(String.format("Missing expression before: '%s'", operator.lexeme()), peek().line());

            Expr right = parseFactor();
            if (right == null)
                reportError(String.format("Missing expression after: '%s'", operator.lexeme()), peek().line());
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr parseAssignment() {
        if (match(TokenType.IDENTIFIER)) {
            Token identifier = previous();
            if (match(TokenType.EQUAL)) {
                Expr right = parseExpression();
                if (right == null)
                    reportError("Expected expression after '='", peek().line());
                return new Expr.Assignment(identifier, right);
            }
            curr--;
            return parseTerm();
        }
        return parseTerm();
    }

    private Expr parseExpression() {
        return parseAssignment();
    }

    Expr parse() throws ParseError {
        Expr ast = parseExpression();
        if (hadError)
            throw new ParseError();
        return ast;
    }

    private void reportError(String msg, int line) {
        Main.reportError("Parse Error " + msg, line);
        hadError = true;
    }

    private Token advance() {
        return tokens.get(curr++);
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.END;
    }

    private Token peek() {
        return tokens.get(curr);
    }

    private Token previous() {
        return tokens.get(curr - 1);
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type() == type;
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
}