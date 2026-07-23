 package com.yen.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
  public static void main(String args[]) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];
    /*  Defining the main AST class that contains subclasses for each expression type.
     *  we'll do that using a script to generate each subclass, its constructor and its fields declartations */
    defineAst(outputDir, "Expr", Arrays.asList(
          "Assign   : Token name, Expr value",
          "Binary   : Expr left, Token operator, Expr right",
          "Call     : Expr callee, Token paren, List<Expr> arguments",
          "Grouping : Expr expression",
          "Unary    : Token operator, Expr right",
          "Logical  : Expr left, Token operator, Expr right",
          "Literal  : Object value",
          "Variable : Token name"
          ));

    defineAst(outputDir, "Stmt", Arrays.asList(
          "Block      : List<Stmt> statements",
          "Expression : Expr expression",
          "Function   : Type type, Token name, List<Parameter> params, List<Stmt> body",
          "If         : Expr condition, Stmt thenBranch,"
                        + " Stmt elseBranch", // the plus is for string concat, to not get a single long line like this one lol.
          "Print      : Expr expression",
          "Return     : Token keyword, Expr value",
          "Var        : Type type, Token name, Expr initializer",
          "While      : Expr condition, Stmt body"
          ));
  }

      private static void defineAst(String outputDir, String baseName,
      List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";

    PrintWriter writer = new PrintWriter(path, "UTF-8");


    writer.println("package com.yen.compiler;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");

    defineVisitor(writer, baseName, types);


    for (String type : types) {
      String className  =  type.split(":")[0].trim();
      String fields  = type.split(":")[1].trim();
      defineType(writer, baseName,  className, fields);
    }

      writer.println();
      writer.println("  abstract <R> R accept(Visitor<R> visitor);");

    writer.println();
    writer.println("}");
    writer.close();
  }

  private  static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
      writer.println("  interface Visitor<R> {");

      for (String type : types) {
        String typeName = type.split(":")[0].trim();
        writer.println("    R   visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
      }


      writer.println("  }");
  }
  
  private static void defineType(PrintWriter  writer, String baseName, String className, String fieldsList) {
    writer.println("    static class "  + className + " extends " + baseName + " {");
      
    writer.println("    " + className + "(" + fieldsList + ") " + "{" );
    String fields[] = fieldsList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1].trim();
      writer.println("      this." + name + " = " + name + ";");
    }
    writer.println("}");

    writer.println();
    writer.println("     @Override");
    writer.println("<R> R accept(Visitor<R> visitor) {");
    writer.println("return visitor.visit" + className  +  baseName + "(this);");
    writer.println("    }");


    writer.println();
    for (String  field : fields) {
      writer.println("    final " + field + ";");
    }
    writer.println("}");


  }
}
