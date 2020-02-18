package pw.mintsoup.lmao.parser;

import jdk.nashorn.internal.runtime.ParserException;
import pw.mintsoup.lmao.Main;
import pw.mintsoup.lmao.scanner.Token;
import pw.mintsoup.lmao.scanner.TokenType;

import java.util.List;

public class Parser {

    public class ParserError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private final AstPrinter printer = new AstPrinter();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParserError e) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression e = comparison();
        while (match(TokenType.NOT_EQUAL, TokenType.DBL_EQUAL)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, comparison(), operator);
        }

        return e;
    }

    private Expression comparison() {
        Expression e = addition();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, addition(), operator);
        }

        return e;
    }

    private Expression multiplication() {
        Expression e = unary();


        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, unary(), operator);
        }
        return e;
    }


    private Expression addition() {
        Expression e = multiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, multiplication(), operator);
        }
        return e;
    }

    private Expression unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = peek(0);

            return new Expression.Unary(operator, unary());
        } else return primary();
    }

    private Expression primary() {

        if (match(TokenType.NUMBER, TokenType.STRING)) return new Expression.Literal(peek(0).literal);
        else if (match(TokenType.TRUE)) return new Expression.Literal(true);
        else if (match(TokenType.FALSE)) return new Expression.Literal(false);
        else if (match(TokenType.NUL)) return new Expression.Literal(null);
        else if (match(TokenType.LEFT_PRNTH)) {
            Expression e = expression();
            if (!match(TokenType.RIGHT_PRNTH)) {
                throw error(peek(), "Unclosed '('");
            }
            return new Expression.Grouping(e);
        }
        throw error(peek(0), "No expression found");
    }

    private void skipStatement() {
        next();
        while (peek(0).type != TokenType.SEMICOLON) {
            switch (peek().type) {
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                case FOR:
                case VAR:
                case FUNC:
                case CLASS:

            }
        }
        next();
        return;
    }

    private ParserError error(Token token, String s) {
        Main.report(token.line, "at '" + token.lex + "'", s);
        throw new ParserError();
    }

    private Token next() {
        current++;
        return peek(0);
    }

    private boolean check(TokenType t) {
        if (isAtEnd()) return false;
        else return t == peek().type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return peek(1);
    }

    private Token peek(int howMany) {
        return tokens.get(current + howMany - 1);
    }

    private boolean match(TokenType... t) {
        for (TokenType type : t) {
            if (check(type)) {
                next();
                return true;
            }
        }
        return false;
    }
}
