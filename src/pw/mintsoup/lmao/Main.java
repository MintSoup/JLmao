package pw.mintsoup.lmao;

import pw.mintsoup.lmao.parser.AstPrinter;
import pw.mintsoup.lmao.parser.Expression;
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

        AstPrinter a = new AstPrinter();
        a.print(new Expression.Binary(
                new Expression.Grouping(
                        new Expression.Binary(
                                new Expression.Literal(13),
                                new Expression.Literal(17),
                                new Token(TokenType.PLUS, null, "+", 0)
                        )
                ),
                new Expression.Grouping(
                        new Expression.Binary(
                                new Expression.Literal(12),
                                new Expression.Unary(
                                        new Token(TokenType.MINUS, null, "-", 0),
                                        new Expression.Literal(13.2)
                                ), new Token(TokenType.PLUS, null, "+", 0)
                        )
                ),
                new Token(TokenType.STAR, null, "-", 0)
        ));


//        if (args.length == 0) {
//            runPrompt();
//        } else {
//            File f = new File(args[0]);
//            if (!f.exists()) {
//                System.out.println("Cannot find sourfile " + f.getName());
//                System.exit(64);
//            } else runFile(f);
//        }

    }

    private static void runPrompt() throws IOException {
        InputStreamReader a = new InputStreamReader(System.in);
        BufferedReader b = new BufferedReader(a);

        while (true) {
            System.out.print("> ");
            run(b.readLine());
            hadError = false;
        }
    }

    private static void runFile(File f) throws IOException {
        byte[] source = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
        run(new String(source, Charset.defaultCharset()));
        if (hadError) System.exit(1);
    }

    private static void run(String s) {
        Scanner scanner = new Scanner(s);
        List<Token> tokens = scanner.scanTokens();
        for (Token t : tokens) {
            System.out.println(t);
        }
    }


    private static void report(int line, String where, String message) {
        System.err.printf("[ERROR] Line %d, %s: %s\n", line, where, message);

    }

    public static void error(int line, String message) {
        report(line, "", message);
        hadError = true;


    }
}
