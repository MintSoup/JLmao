package pw.mintsoup.lmao.parser;

import pw.mintsoup.lmao.Main;
import pw.mintsoup.lmao.scanner.Token;
import pw.mintsoup.lmao.scanner.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    public class ParserError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private final List<Statement> statements = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


    public List<Statement> parse() {
        try {

            while (!isAtEnd()) statements.add(declaration());
            return statements;
        } catch (ParserError e) {
            skipStatement();
            return null;
        }
    }

    private Statement declaration() {
        try {
            if (match(TokenType.LET)) {
                return varDeclaration();

            } else if (match(TokenType.FUNC)) {
                return functionDeclaration("function");
            } else if (match(TokenType.CLASS)) {
                return classDeclaration();
            } else {
                return statement();
            }
        } catch (ParserError e) {
            skipStatement();
            return null;
        }
    }

    private Statement classDeclaration() {
        Token name = next();
        consume(TokenType.LEFT_BRACE, "Expected '{' after class declaration");
        List<Statement.Function> functions = new ArrayList<>();
        while (match(TokenType.FUNC) && !isAtEnd()) functions.add(functionDeclaration("method"));
        consume(TokenType.RIGHT_BRACE, "Expected '}' for closing class declaration");
        return new Statement.Class(name, functions);
    }

    private Statement.Function functionDeclaration(String type) {
        Token name = consume(TokenType.IDENTIFIER, "Expected identifier for " + type + "declaration");
        consume(TokenType.LEFT_PRNTH, "Expected '(' after" + type + " declaration");
        List<Token> params = new ArrayList<>();
        if (!match(TokenType.RIGHT_PRNTH)) {
            while (true) {
                params.add(next());
                if (match(TokenType.RIGHT_PRNTH)) break;
                else consume(TokenType.COMMA, "Expected ',' after " + type + " parameter");
            }
        }
        consume(TokenType.LEFT_BRACE, "Expected '{' after " + type + " declaration");
        List<Statement> body = block();
        return new Statement.Function(name, params, body);
    }

    private Token consume(TokenType t, String message) {
        if (peek().type == t) return next();
        else throw error(peek(0), message);
    }

    private Statement varDeclaration() {
        Token name = next();
        if (name.type != TokenType.IDENTIFIER) {
            throw error(name, "Expected identifier for variable name.");
        }
        Expression init = null;
        if (match(TokenType.EQUAL)) {
            init = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");

        return new Statement.Var(name, init);
    }

    private Statement statement() {
        if (match(TokenType.PRINT)) {
            Expression f = expression();
            consume(TokenType.SEMICOLON, "Excepted ';' after print.");

            return new Statement.Print(f);

        } else if (match(TokenType.IF)) {
            return ifStatement();
        } else if (match(TokenType.WHILE)) {
            return whileStatement();
        } else if (match(TokenType.FOR)) {
            return forStatement();
        } else if (match(TokenType.RETURN)) {
            return returnStatement(peek(0));
        } else if (match(TokenType.BREAK)) {
            consume(TokenType.SEMICOLON, "';' expected after break/continue.");
            return new Statement.Break(peek(-1));
        } else if (match(TokenType.LEFT_BRACE)) {
            return new Statement.Block(block());
        } else return expressionStatement();

    }

    private Statement returnStatement(Token tok) {
        Expression e;
        if (match(TokenType.SEMICOLON))
            e = null;
        else {
            e = expression();
            consume(TokenType.SEMICOLON, "Expected ';' after return statement");
        }
        return new Statement.Return(e, tok);
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PRNTH, "Expected '(' after while statement.");

        Statement init;
        if (match(TokenType.SEMICOLON)) init = null;
        else if (match(TokenType.LET)) init = varDeclaration();
        else init = expressionStatement();


        Expression condition;
        if (match(TokenType.SEMICOLON)) condition = null;
        else {
            condition = expression();
            consume(TokenType.SEMICOLON, "Expected ';' after condition of a for loop");

        }

        Expression increment;
        if (match(TokenType.RIGHT_PRNTH)) increment = null;
        else {
            increment = expression();
            consume(TokenType.RIGHT_PRNTH, "Expected ')' for loop");

        }

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(Arrays.asList(body, new Statement.EStatement(increment)));
        }
        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);

        if (init != null)
            body = new Statement.Block(Arrays.asList(init, body));


        return body;
    }

    private Statement.While whileStatement() {

        consume(TokenType.LEFT_PRNTH, "Expected '(' after while statement.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PRNTH, "Expected ')' after while statement.");
        Statement stmt = statement();
        return new Statement.While(condition, stmt);
    }

    private Statement.If ifStatement() {
        consume(TokenType.LEFT_PRNTH, "Expected '(' after if statement.");

        Expression condition = expression();
        consume(TokenType.RIGHT_PRNTH, "Expected ')' after if statement.");

        Statement stmt = null;
        Statement els = null;
        stmt = statement();
        if (match(TokenType.ELSE)) els = statement();
        return new Statement.If(condition, stmt, els);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd() && !match(TokenType.RIGHT_BRACE)) {
            statements.add(declaration());
        }

        if (peek(0).type != TokenType.RIGHT_BRACE) throw error(peek(0), "Unmatched curly braces");
        return statements;
    }

    private Statement expressionStatement() {
        Expression e = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");

        return new Statement.EStatement(e);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression e = logical();

        if (match(TokenType.EQUAL)) {
            Token equals = peek(0);
            Expression value = assignment();

            if (e instanceof Expression.Variable) {
                Token name = ((Expression.Variable) e).name;
                return new Expression.Assignment(name, value, null);
            } else if (e instanceof Expression.Map) {
                Token name = ((Expression.Map) e).nameToken;
                return new Expression.Assignment(name, value, ((Expression.Map) e).index);
            } else if (e instanceof Expression.Get) {
                e = new Expression.Set(((Expression.Get) e).object, ((Expression.Get) e).name, value);
            } else {
                error(equals, "Invalid assignment target.");
            }
        }


        return e;
    }

    private Expression logical() {
        Expression e = equality();

        while (match(TokenType.AND, TokenType.OR, TokenType.XOR)) {
            Token operator = peek(0);
            e = new Expression.Logical(e, equality(), operator);
        }
        return e;
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
        Expression e = modulo();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, addition(), operator);
        }

        return e;
    }

    private Expression modulo() {
        Expression e = addition();

        while (match(TokenType.MODULO)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, addition(), operator);
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

    private Expression multiplication() {
        Expression e = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = peek(0);
            e = new Expression.Binary(e, unary(), operator);
        }
        return e;
    }

    private Expression unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = peek(0);
            return new Expression.Unary(operator, unary());
        } else return call();

    }

    private Expression call() {
        Expression e = primary();
        while (true) {
            if (match(TokenType.LEFT_PRNTH)) e = finishCall(e);
            else if (match(TokenType.LEFT_SBRACKET)) e = finishMap(e, peek(-1));
            else if (match(TokenType.DOT)) {
                e = new Expression.Get(e, consume(TokenType.IDENTIFIER, "Expected identifier after '.'."));
            } else break;
        }

        return e;
    }

    private Expression finishMap(Expression e, Token nameToken) {
        Expression index = expression();
        consume(TokenType.RIGHT_SBRACKET, "Expected ']' after index expression.");
        return new Expression.Map(e, index, nameToken);
    }

    private Expression finishCall(Expression e) {
        List<Expression> args = new ArrayList<>();
        if (!match(TokenType.RIGHT_PRNTH)) {
            do {
                args.add(expression());
            } while (match(TokenType.COMMA));
            if (next().type != TokenType.RIGHT_PRNTH) error(peek(0), "Expected ')' after function arguments.");
        }

        if (args.size() > 255)
            throw error(peek(0), "Function takes " + args.size() + " arguments, max 255 supported");

        return new Expression.Call(e, peek(0), args);
    }

    private Expression primary() {

        if (match(TokenType.NUMBER, TokenType.STRING)) return new Expression.Literal(peek(0).literal);
        else if (match(TokenType.TRUE)) return new Expression.Literal(true);
        else if (match(TokenType.FALSE)) return new Expression.Literal(false);
        else if (match(TokenType.NUL)) return new Expression.Literal(null);
        else if (match(TokenType.THIS)) return new Expression.This(peek(0));
        else if (match(TokenType.LEFT_PRNTH)) {
            Expression e = expression();
            consume(TokenType.RIGHT_PRNTH, "Expected ')' after grouping expression.");
            return new Expression.Grouping(e);
        } else if (match(TokenType.IDENTIFIER)) return new Expression.Variable(peek(0));
        throw error(peek(0), "No expression found");
    }

    private void skipStatement() {
        next();
        if (!isAtEnd())
            while (peek(0).type != TokenType.SEMICOLON) {
                switch (peek().type) {
                    case IF:
                    case WHILE:
                    case PRINT:
                    case RETURN:
                    case FOR:
                    case LET:
                    case FUNC:
                    case CLASS:

                }
                next();
            }
        return;
    }

    private ParserError error(Token token, String s) {
        Main.error(token.line, s);
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
        return current + 1 >= tokens.size();
    }

    private Token peek() {
        return peek(1);
    }

    private Token peek(int howMany) {
        try {
            return tokens.get(current + howMany - 1);
        } catch (IndexOutOfBoundsException e) {
            try {
                Main.report(tokens.get(current - 1).line, "", "not enough tokens");
            } catch (IndexOutOfBoundsException f) {
                Main.report(tokens.get(current - 2).line, "", "not enough tokens");
            }
            throw new ParserError();
        }
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
