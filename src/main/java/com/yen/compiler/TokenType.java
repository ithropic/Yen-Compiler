package com.yen.compiler;

enum TokenType {
  // single character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, SEMICOLON, PLUS, MINUS, STAR, SLASH,

  // one or two character tokens.
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  LESS, LESS_EQUAL,
  GREATER, GREATER_EQUAL,

  // literals.
  IDENTIFIER, STRING_LITERAL, INT_LITERAL, DOUBLE_LITERAL,

  //keywords.
  AND, OR, IF, ELSE, TRUE, FALSE, VOID, INT,
  DOUBLE, BOOL, PRINT, RETURN, FOR, WHILE, STRING,

  EOF 
}
