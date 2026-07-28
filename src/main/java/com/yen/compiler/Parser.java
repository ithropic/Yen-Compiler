package com.yen.compiler;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static com.yen.compiler.TokenType.*;


class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0; // index used to track the current Token in tokens.

  Parser(List<Token> tokens) {
    this.tokens = tokens; // constructor.
  }

  List<Stmt> parse()  { 
    // an expression evaluates to some value, it is not an instruction
    // "1 + 1, x + 2".
     // a statement is a full instruction that perform an action. 
     // "if statement, while loop, variable declaration."
     // an expression statement is an expression folowed by a semicolon, 
     // it is an expression that produces a side effect.
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  private Stmt declaration() {
    try {
    if (match(TokenType.INT, TokenType.DOUBLE, TokenType.STRING, TokenType.BOOL, TokenType.VOID))
        {
          Type type = getType(previous());
          Token name = consume(IDENTIFIER, "Expect identifier in a declaration.");
          if (check(LEFT_PAREN)) {
              return funDecl(type, name);
          } else {
            return varDeclaration(type, name);
          }
        }
    
    return statement();
    } catch (ParseError error){
      synchronize();
      return null;
    }
  }

  private Stmt statement() {
    if (match(FOR)) return forStatement();
    if (match(IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(RETURN)) return returnStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after for.");
    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    }
     else if (matchType()) {
       Type type = getType(previous());
       Token name = consume(IDENTIFIER, "Expect identifier after type.");
      initializer = varDeclaration(type, name);
    } 
    else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");

    Stmt body = statement();
    // "desugaring" transforming the for loop into a while loop because we practically
    // dont need for loops they are just somtimes nicer to write (syntaxic sugar)."
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
      // if there is an increment expression add it to the end of the body.
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }
    
    return body;
  }
  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect '(' after if.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr value = expression(); // value will contain the AST
    // of the expression being printed.
    consume(SEMICOLON, "Expect ';' after expression");
    return new Stmt.Print(value);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    
    Expr value = null;
    if (!check(SEMICOLON)) {
       value = expression();
    }

    consume(SEMICOLON, "Expect ';' after return statement.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt varDeclaration(Type type, Token name) {
    
    Expr initializer = null;
    if (match(EQUAL)) {
     initializer = expression();
    }
    consume(SEMICOLON, "Expect ';' after variable declaration.");

    return new Stmt.Var(type, name, initializer);
  }

  private Stmt.Function funDecl(Type type, Token name) {
    consume(LEFT_PAREN, "Expect '(' after function name."); // this error is never going to happen btw.
    List<Parameter> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Arguments limit reached (255 max).");
        }
        Type paramType = getType(peek());
        if (!matchType()) {
          throw error(peek(), "Expect parameter type.");
        }
        Token paramName = consume(IDENTIFIER, "Expect parameter name.");
        parameters.add(new Parameter(paramType, paramName));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");

      consume(LEFT_BRACE, "Expect '{' before function body.");
      List<Stmt> body = block();
      return new Stmt.Function(type, name, parameters, body);
  }


  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after while.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body); 
  }

  private Stmt expressionStatement() {
  Expr expr = expression();
  consume(SEMICOLON, "Expect ';' after expression");
  return new Stmt.Expression(expr);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private Expr assignment() {
    Expr  expr = or(); // get left side as an expression temporarily.

    if (match(EQUAL)) {
      Token equal = previous();
      Expr value = assignment(); // it is possible to have multiple cascaded
                             // assignments if not, it returns an expression.

      if (expr instanceof Expr.Variable) { // if the left side is not a variable
                                           // of assignable, error.
        Token name = ((Expr.Variable)expr).name; // now the left side expr is treated
                                                 // as a variable,not an expression
                                                 // to be evaluated but a location.
        return new Expr.Assign(name, value);
      }
      error(equal, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
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
    return call();
  }

  private Expr finishCall(Expr.Variable callee) {
    List<Expr> arguments = new ArrayList<>();
      if (!check(RIGHT_PAREN)) {
        do {
          if (arguments.size() >= 255) {
            error(peek(), "Arguments limit reached (255 max).");
          }
        arguments.add(expression());
      } while (match(COMMA));
    }
      
    Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments); 
  }
    
  private Expr call() {
    Expr expr = primary();

      if (match(LEFT_PAREN)) {
        if (!(expr instanceof Expr.Variable calleeVar)) {
          error(previous(), "Function can only be called by name.");
          return expr;
        }
        expr = finishCall(calleeVar);
      }

    return expr;
  }

  private Expr primary() { // if match found then create new Expr node of type Literal.
    if (match(INT_LITERAL, DOUBLE_LITERAL, STRING_LITERAL)) return new Expr.Literal(previous().literal); 
    if (match(TRUE))  return new Expr.Literal(true);
    if (match(FALSE))  return new Expr.Literal(false);
    if (match(IDENTIFIER)) return new Expr.Variable(previous());

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

  private Token consumeType(String message) { // consume method to handle
                                              // static Types.
    if (matchType()) {
      return previous();
    }

    throw error(peek(), message);
  }

  private boolean matchType() {
    if (getType(peek()) != Type.ERROR) {
      advance();
      return true;
    }

    return false;
  }

  private Type getType(Token token) {
    switch(token.type) {
      case INT    : return Type.INT;
      case DOUBLE : return Type.DOUBLE;
      case STRING : return Type.STRING;
      case BOOL   : return Type.BOOL;
      case VOID   : return Type.VOID;
      default : return Type.ERROR;
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
