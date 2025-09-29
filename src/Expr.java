import java.util.Hashtable;

abstract class Expr {

    abstract double interpret(Hashtable<String, Double> identifierTable) throws RuntimeError;

    public static class RuntimeError extends Exception {}

    static class Identifier extends Expr {

        Token identifier;

        Identifier(Token identifier) {
            this.identifier = identifier;
        }

        @Override
        double interpret(Hashtable<String, Double> identifierTable) throws RuntimeError {
            Double ret = identifierTable.get(identifier.lexeme());
            if (ret == null) {
                Main.reportError(String.format("Unresolved identifier: '%s'", identifier.lexeme()), identifier.line());
                throw new RuntimeError();
            }
            return ret;
        }
    }

    static class Assignment extends Expr {
        Token identifier;
        Expr right;

        Assignment(Token identifier, Expr right) {
            this.identifier = identifier;
            this.right = right;
        }

        @Override
        double interpret(Hashtable<String, Double> identifierTable) throws RuntimeError {
            double value = right.interpret(identifierTable);
            identifierTable.put(identifier.lexeme(), value);
            return value;
        }
    }

    static class Binary extends Expr {
        Expr left;
        Token operator;
        Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        double interpret(Hashtable<String, Double> identifierTable) throws RuntimeError {
            double a = left.interpret(identifierTable);
            double b = right.interpret(identifierTable);

            if (operator.type() == TokenType.DIVIDE && b == 0) {
                Main.reportError("RunTime Error: Can't divide by zero", operator.line());
                throw new RuntimeError();
            }

            return switch (operator.type()) {
                case MINUS -> a - b;
                case PLUS -> a + b;
                case MULTIPLY -> a * b;
                case DIVIDE -> a / b;
                case POWER -> Math.pow(a, b);
                default -> 0;
            };
        }
    }

    static class Unary extends Expr {
        Token operator;
        Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        double interpret(Hashtable<String, Double> identifierTable) throws RuntimeError {
            return -right.interpret(identifierTable);
        }
    }

    static class Literal extends Expr {
        Token literal;

        Literal(Token literal) {
            this.literal = literal;
        }

        @Override
        double interpret(Hashtable<String, Double> identifierTable) {
            return literal.value();
        }
    }
}