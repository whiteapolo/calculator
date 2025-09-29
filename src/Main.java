import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

public class Main {

    static void reportError(String msg, int line) {
        System.out.println(msg + ": line " + line);
    }

    static double solveExpression(final String expr, Hashtable<String, Double> identifierTable) throws Lexer.SyntaxError, Parser.ParseError, Expr.RuntimeError {
        Lexer lexer = new Lexer(expr);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        Expr ast = parser.parse();
        return ast.interpret(identifierTable);
    }

    static void printResult(final double d) {
        if (d == (long)d)
            System.out.printf("%,d\n", (long)d);
        else
            System.out.printf("%,f\n", d);
    }

    static void interpretLine(final String line, Hashtable<String, Double> identifierTable) {
        try {
            final double result = solveExpression(line, identifierTable);
            identifierTable.put("$", result);
            printResult(result);
        } catch (Exception e) {
            // do nothing
            // continue to next expression
        }
    }

    static void runPrompt(final String prompt) {
        final Scanner scanner = new Scanner(System.in);
        Hashtable<String, Double> identifierTable = new Hashtable<>();
        identifierTable.put("$", 0.0);

        System.out.print(prompt);

        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            if (!line.isEmpty())
                interpretLine(line, identifierTable);
            System.out.print(prompt);
        }

        scanner.close();
    }

    static int runFile(final String fileName) {
        File file = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.printf("File '%s' not found.\n", file);
            return 0;
        }

        Hashtable<String, Double> identifierTable = new Hashtable<>();
        identifierTable.put("$", 0.0);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            if (!line.isEmpty()) {
                System.out.printf("[ %s ] -> ", line);
                try {
                    final double result = solveExpression(line, identifierTable);
                    printResult(result);
                    identifierTable.put("$", result);
                } catch (Exception e) {
                    scanner.close();
                    return 1;
                }
            }
        }

        scanner.close();
        return 0;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            runPrompt("> ");
            System.exit(0);
        } else if (args.length == 1) {
            final int exitCode = runFile(args[0]);
            System.exit(exitCode);
        } else {
            System.out.println("Usage calculator [script]");
            System.exit(1);
        }
    }
}
