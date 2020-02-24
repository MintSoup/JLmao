package pw.mintsoup.lmao.scanner;

public enum TokenType {
    //Single-Character
    RIGHT_PRNTH, LEFT_PRNTH, PLUS, MINUS, SLASH, STAR, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON,

    //Single/Double
    NOT, NOT_EQUAL,
    EQUAL, DBL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,


    //Literals
    STRING, IDENTIFIER, NUMBER,

    //Keywords
    IF, ELSE, AND, CLASS, OR, WHILE, FOR, FUNC, TRUE, FALSE, LET, RETURN ,SUPER, PRINT, THIS, NUL, XOR,


    //Other
    EOF
}
