package core.lexer

enum class TokenType{
    NUMBER,

    ID,
    STRING,
    VAR,

    PRINT,

    IF, ELSE,
    WHILE,
    TRUE, FALSE,

    // Operators
    PLUS, MINUS, STAR, SLASH,  // + - * /
    EQ, EQEQ, EXCL, NEQ,       // = == ! !=
    LT, GT, LTEQ, GTEQ,        // < > <= >=
    AND, OR,                   // && ||

    LPAREN, RPAREN,     // ( )
    LBRACE, RBRACE,     // { }
    SEMICOLON,          // ;
    COLON,              // :

    TYPE_NUMBER,        // Number
    TYPE_STRING,        // String
    TYPE_BOOLEAN,       // Boolean

    EOF
}
