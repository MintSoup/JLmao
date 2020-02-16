package pw.mintsoup.lmao.scanner;

public class Token {
    final TokenType type;
    final Object literal;
    String lex;
    int line;

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
        }
        else return "Token{type=" + type + "}";
    }
}
