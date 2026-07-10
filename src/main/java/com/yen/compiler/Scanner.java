package com.yen.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.yen.compiler.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source)
  {
    this.source = source;
  }

  List<Token> scanTokens() 
  {
    while (!isAtEnd())
    {
      start = current; // pointing to the beginning of the next lexeme.
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(' : addToken(LEFT_PAREN); break;
      case ')' : addToken(RIGHT_PAREN); break;
      case '{' : addToken(LEFT_BRACE); break;
      case '}' : addToken(RIGHT_BRACE); break;
      case ',' : addToken(COMMA); break;
      case '.' : addToken(DOT); break;
      case ';' : addToken(SEMICOLON); break;
      case '+' : addToken(PLUS); break;
      case '-' : addToken(MINUS); break;
      case '*' : addToken(STAR); break;

      case '!' : 
                addToken(match('=')  ? BANG_EQUAL : BANG);
                break;
      case '=' :
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
      case '<' : 
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
      case '>' : 
                addToken(match('=') ? GREATER_EQUAL :  GREATER);
                break;
      case '/' :
                if (match('/')) {
                  while(peek() != '\n' && !isAtEnd()) advance(); // here  advance will be returning each char it 
                                                                  // encounters, but java will simply ignore them
                                                                  // since they aren't assingned to anything.
                } 
                else {
                  addToken(SLASH);
                }
                break;
      case ' ' :
      case '\t':
      case '\r':
                break; // white spaces are not added. current will keep advancing 
                        // as long as there is a white space.
      case '\n':
                line++; break;

      default: 
        Compiler.error(line, "Unexpected character.");
        break;
    }
  }

  private boolean isAtEnd()
  {
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

  private void addToken(TokenType type, Object literal) { // this method is called  directly when the token is a literal.
  // (numbers, strings..) so the literal/value gets stored correcly instead of null.
    String text = source.substring(start, current); // passing curent instead of current -1 
    // comes down to how java's String.substring(); works, it returns the substring from 
    // the start index until the index right before endIndex so the last char (pointed by tha enbIndex arg)
    // is not included.
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char c) {
    if (isATEnd()) return false;
    
    if (source.charAt(current)  != c) return false;

    current++; // we only move to the next char if there is a match because current
              // and the previous will be the same token.
              // if there is no match the the previous will be tokened separately
              // and current separately, not the same token.
    return true;
  }

  private char peek()
  {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

}


