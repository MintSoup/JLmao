package pw.mintsoup.lmao.scanner;

import pw.mintsoup.lmao.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pw.mintsoup.lmao.scanner.TokenType.*;

public class Scanner {


    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private static final Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    int line;
    int start;
    int current;


    static {
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("func", FUNC);
        keywords.put("if", IF);
        keywords.put("null", NUL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("let", LET);
        keywords.put("while", WHILE);
        keywords.put("xor", XOR);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scan();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scan() {
        char c = next();
//        RIGHT_PRNTH, LEFT_PRNTH, PLUS, MINUS, SLASH, STAR, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON,
        switch (c) {
            case ')': {
                addToken(RIGHT_PRNTH);
                break;
            }
            case '(': {
                addToken(LEFT_PRNTH);
                break;
            }
            case '+': {
                addToken(PLUS);
                break;
            }
            case '-': {
                addToken(MINUS);
                break;
            }
            case '*': {
                addToken(STAR);
                break;
            }
            case '{': {
                addToken(LEFT_BRACE);
                break;
            }
            case '}': {
                addToken(RIGHT_BRACE);
                break;
            }
            case ',': {
                addToken(COMMA);
                break;
            }
            case '.': {
                addToken(DOT);
                break;
            }
            case ';': {
                addToken(SEMICOLON);
                break;
            }

            case '!': {
                if (match('=')) {
                    addToken(NOT_EQUAL);
                } else {
                    addToken(NOT);
                }
                break;
            }
            case '>': {
                if (match('=')) {
                    addToken(GREATER_EQUAL);
                } else {
                    addToken(GREATER);
                }
                break;
            }
            case '<': {
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else {
                    addToken(LESS);
                }
                break;
            }
            case '=': {
                if (match('=')) {
                    addToken(DBL_EQUAL);
                } else {
                    addToken(EQUAL);
                }
                break;
            }

            case '/': {
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') next();
                } else addToken(SLASH);
                break;
            }

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n': {
                line++;
                break;
            }

            case '\"': {
                while (true) {
                    if (isAtEnd()) {
                        Main.error(line, "Unterminated string");
                        throw new ScannerError();
                    }
                    char g = next();
                    if (g == '\"') {
                        break;
                    }
                    if (g == '\n') {
                        line++;
                    }
                }
                String str = source.substring(start + 1, current - 1);
                addToken(STRING, str);
            }


            break;

            default: {
                if (isDigit(c)) {
                    while (isDigit(peek())) next();
                    if (peek() == '.' && isDigit(peek(2))) next();
                    {
                        while (isDigit(peek())) next();
                    }
                    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));

                } else if (isAlpha(c)) {
                    while (!isAtEnd() && isAlphanumeric(peek())) {
                        next();
                    }

                    String text = source.substring(start, current);
                    TokenType type = keywords.get(text);
                    if (type == null) {
                        addToken(IDENTIFIER);
                    } else {
                        addToken(type);

                    }

                } else {
                    Main.error(line + 1, "Unexpected character: " + c);
                }

                break;
            }
        }
    }

    private boolean isAlphanumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean match(char c) {
        if (isAtEnd()) return false;

        if (source.charAt(current) == c) {
            current++;
            return true;
        } else return false;
    }

    private char next() {
        current++;
        return source.charAt(current - 1);
    }

    private char peek() {
        return peek(1);
    }

    private char peek(int n) {
        if (current + n - 1 >= source.length()) return 0;
        return source.charAt(current + n - 1);
    }

    private void addToken(TokenType a) {
        addToken(a, null);
    }

    private void addToken(TokenType a, Object literal) {
        tokens.add(new Token(a, literal, source.substring(start, current), line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    public class ScannerError extends RuntimeException {
    }
}
