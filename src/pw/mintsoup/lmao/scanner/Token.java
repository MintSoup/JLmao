package pw.mintsoup.lmao.scanner;

public class Token {
    public final TokenType type;
    public final Object literal;
    public final String lex;
    public final int line;

    public Token(TokenType type, Object literal, String lex, int line) {
        this.type = type;
        this.literal = literal;
        this.lex = lex;
        this.line = line;
    }

    @Override
    public String toString() {
        if (literal != null) {
            return "Token{" +
                    "type=" + type +
                    ", literal=" + literal +
                    ", lex='" + lex + '\'' +
                    ", line=" + line +
                    '}';
        } else return "Token{type=" + type + "}";
    }
}
