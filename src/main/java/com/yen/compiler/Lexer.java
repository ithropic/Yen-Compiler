package com.yen.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.yen.compiler.TokenType.*;

class Lexer {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("or", OR);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("if", IF);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("true", TRUE);
    keywords.put("while", WHILE);
    keywords.put("int", INT);
    keywords.put("double", DOUBLE);
    keywords.put("bool", BOOL);
    keywords.put("string", STRING);
    keywords.put("void", VOID);
  }

  Lexer(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current; // pointing to the beginning of the next lexeme.
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '+':
        addToken(PLUS);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '*':
        addToken(STAR);
        break;

      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd())
            advance(); // here advance will be returning each char it
                       // encounters, but java will simply ignore them
                       // since they aren't assingned to anything.
        } else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\t':
      case '\r':
        break; // white spaces are not added. current will keep advancing
               // as long as there is a white space.
      case '\n':
        line++;
        break;

      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else {
          if (isAlpha(c)) {
            identifier();
          } else {
            Compiler.error(line, "Unexpected character.");
          }
        }

    }
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) // this method is called when the token is not a literal.
  {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) { // this method is called directly when the token is a literal.
    // (numbers, strings..) so the literal/value gets stored correcly instead of
    // null.
    String text = source.substring(start, current); // passing curent instead of current -1
    // comes down to how java's String.substring(); works, it returns the substring
    // from
    // the start index until the index right before endIndex so the last char
    // (pointed by tha enbIndex arg)
    // is not included.
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char c) {
    if (isAtEnd())
      return false;

    if (source.charAt(current) != c)
      return false;

    current++; // we only move to the next char if there is a match because current
               // and the previous will be the same token.
               // if there is no match the the previous will be tokened separately
               // and current separately, not the same token.
    return true;
  }

  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  private void string() {
    while (!isAtEnd() && peek() != '"') {
      if (peek() == '\n')
        line++;
      advance(); // or current++; which I think is faster.
    }

    if (isAtEnd()) {
      Compiler.error(line, "Undetermined string.");
    }
    advance(); // getting past the closing '"'.

    String text = source.substring(start + 1, current - 1); // to exclude the starting and ending '"'.

    addToken(STRING_LITERAL, text);
  }

  private boolean isDigit(char c) {
    return (c >= '0' && c <= '9');
  }

  private void number() {
    boolean isDouble = false;
    while (isDigit(peek())) {
      advance();
    }
    if (peek() == '.' && isDigit(peekNext())) {
      isDouble = true; // not an INT.
      advance(); // eat the '.'
      while (isDigit(peek()))
        advance();
    }

    if (isDouble) {
      addToken(DOUBLE_LITERAL, Double.parseDouble(source.substring(start, current)));
    } else {
      addToken(INT_LITERAL, Integer.parseInt(source.substring(start, current)));
    }
  }

  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';

    return source.charAt(current + 1);
  }

  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = IDENTIFIER;

    addToken(type);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

}
