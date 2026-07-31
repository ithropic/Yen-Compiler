package com.yen.compiler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Compiler {
  static boolean hadError = false;
  public static void main(String[] args) throws IOException {
    if (args.length != 1) // the program command "yen" is not counted as an argument
                          // unlike C/C++ so args[0] will be [file].
    {
     System.out.println("Usage: yen [file]");
     System.exit(64);
    }
    else {
     runFile(args[0]); 
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path)); // Paths.get() converts the String into a system path object
                                                        // if it is not an absolute path the OS will look for the file 
                                                        // in the current working directory.
    run(new String(bytes, Charset.defaultCharset())); // UTF-8 by default.

    if (hadError) System.err.println(65);
    
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);

    List<Stmt> statements = parser.parse();
    if (hadError) return;

    Resolver resolver = new Resolver();
    resolver.resolveProgram(statements);
    if (hadError) return;

    TypeChecker typeChecker = new TypeChecker();
    typeChecker.check(statements);
    if (hadError) return;


    new TypeDumper().dump(statements);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[Line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token , String message) {
      if (token.type ==  TokenType.EOF) {
        report(token.line, " at end", message);
      } else {
        report(token.line, " at '" + token.lexeme + "'", message);
      }
    }


}
