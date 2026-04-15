package core.lexer

enum class TokenType{
    NUMBER,
    BOOLEAN,

    ID,
    STRING,
    VAR,

    PRINT,

    IF, ELSE,
    WHILE,

    // Operators
    PLUS, MINUS, STAR, SLASH,  // + - * /
    EQ, EQEQ, EXCL, NEQ,       // = == ! !=
    LT, GT, LTEQ, GTEQ,        // < > <= >=
    AND, OR,                   // && ||

    LPAREN, RPAREN,     // ( )
    LBRACE, RBRACE,     // { }
    COLON,              // :
    SEMICOLON,          // ;

    EOF
}