package pw.mintsoup.lmao;

import pw.mintsoup.lmao.parser.AstPrinter;
import pw.mintsoup.lmao.parser.Expression;
import pw.mintsoup.lmao.parser.Parser;
import pw.mintsoup.lmao.parser.Statement;
import pw.mintsoup.lmao.scanner.Scanner;
import pw.mintsoup.lmao.scanner.Token;
import pw.mintsoup.lmao.scanner.TokenType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static boolean hadError;

    public static void main(String[] args) throws IOException {


        if (args.length == 0) {
            runPrompt();
        } else {
            File f = new File(args[0]);
            if (!f.exists()) {
                System.out.println("Cannot find sourfile " + f.getName());
                System.exit(64);
            } else runFile(f);
        }

    }

    private static void runPrompt() throws IOException {
        InputStreamReader a = new InputStreamReader(System.in);
        BufferedReader b = new BufferedReader(a);

        while (true) {
            System.out.print("> ");
            String line = b.readLine();
            run(line);
            hadError = false;
        }
    }

    private static void runFile(File f) throws IOException {
        byte[] source = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
        run(new String(source, Charset.defaultCharset()));
        if (hadError) System.exit(1);
    }

    private static void run(String s) {
        try {
            Scanner scanner = new Scanner(s);
            List<Token> tokens = scanner.scanTokens();
            Parser p = new Parser(tokens);
            Interpreter i = new Interpreter();
            List<Statement> statements = p.parse();
            if (statements != null) {
                for (Statement statement : statements) {
                    if(statement != null)
                    statement.accept(i);
                }
            }

        } catch (Interpreter.InterpreterError e) {

        } catch (Scanner.ScannerError e) {

        } catch(Environment.EnvironmentError e) {

        }


    }


    public static void report(int line, String where, String message) {
        System.err.printf("[ERROR] Line %d, %s: %s\n", line, where, message);

    }

    public static void error(int line, String message) {
        report(line, "", message);
        System.err.flush();

        hadError = true;
    }
}
