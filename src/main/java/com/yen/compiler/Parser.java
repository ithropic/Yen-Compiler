package com.yen.compiler;

import java.util.List;

import static com.yen.compiler.TokenType.*;


class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0; // index used to track the current Token in tokens.

  Parser(List<Token> tokens) {
    this.tokens = tokens; // constructor.
  }

  Expr parse()  {
    try  {
      return expression(); // recursive descent call --start.
    } catch (ParseError error)  {
      return null;
    }
  }

    // multiple recursive calls mirroring the laguage's grammar rules.
  private Expr expression() {
    return equality();
  }

  

  private Expr equality() {
  Expr left = comparison();

  while (match(BANG_EQUAL, EQUAL_EQUAL)) {
    Token operator = previous();
    Expr right = comparison();
    left = new Expr.Binary(left, operator, right);
  }
  return left;
  }

  private Expr comparison() {
    Expr left = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      left = new Expr.Binary(left, operator, right);
    }
    return left;
  }

  private Expr term() {
    Expr left = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      left = new Expr.Binary(left, operator, right);
    }
    return left;
  }

  private Expr factor() {
    Expr left = unary();
    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      left = new Expr.Binary(left, operator, right);
    }
    return left;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    } 
    return primary();
  }

  private Expr primary() { // if match found then create new Expr node of type Literal.
    if (match(INT_LITERAL, DOUBLE_LITERAL, STRING)) return new Expr.Literal(previous().literal); 
    if (match(TRUE))  return new Expr.Literal(true);
    if (match(FALSE))  return new Expr.Literal(false);

    if (match(LEFT_PAREN)) {
      Expr exp = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression."); // after we finish  parsing the inner expression 
                                                            // we must find a closing ')', else "syntax Error"
      return new Expr.Grouping(exp);
    }
    throw error(peek(), "Expect expression."); // if we arrive at primary and none of the expected types
                                               // is found, it is and invalid sequence like: "+ 5", (1 + ).
  }

  // helpers.
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) 
      {
        advance();
        return true; 
      }
    }
    return false;
  }

  private boolean check(TokenType type)  {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
   
  }
  private ParseError error(Token token, String message) {
    Compiler.error(token, message);
    return new ParseError();
  }

  // when an error occurs we throw an error and catch it at the first calling method "parse()" 
  // to pop the current recursive calls stack frame and report the error but the parser keeps going
  // inorder to find other eventual errors.
  // for this we need to synchronize the parser, i.e. we need to skip the tokens
  // that were going to be processed in that removed stack frame.
  // to do that we keep skipping tokens until we reach something that indicates
  // the end of the current statement containing the error and the start of a new 
  // statement something like: a SEMICOLON, type initializers or return types like INT, 
  // VOID, or a for, while loops...
 
  private void synchronize() {
  advance();
  while (!isAtEnd()) {
    if (previous().type == SEMICOLON) return;

    switch(peek().type) {
      case FOR:
      case IF:
      case WHILE:
      case PRINT:
      case RETURN:
      case INT:
      case DOUBLE:
      case BOOL:
      case VOID:
      case STRING:
        return;
    }
    advance();
  }
  }

  
}
