package com.yen.compiler;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R   visitBlockStmt(Block stmt);
    R   visitExpressionStmt(Expression stmt);
    R   visitFunctionStmt(Function stmt);
    R   visitIfStmt(If stmt);
    R   visitPrintStmt(Print stmt);
    R   visitReturnStmt(Return stmt);
    R   visitVarStmt(Var stmt);
    R   visitWhileStmt(While stmt);
  }
    static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
}
    static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
}
    static class Function extends Stmt {
    Function(Type type, Token name, List<Parameter> params, List<Stmt> body) {
      this.type = type;
      this.name = name;
      this.params = params;
      this.body = body;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitFunctionStmt(this);
    }

    final Type type;
    final Token name;
    final List<Parameter> params;
    final List<Stmt> body;
    Symbol symbol;
}
    static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch, Token keyword) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
      this.keyword = keyword;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
    final Token keyword;
}
    static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitPrintStmt(this);
    }

    final Expr expression;
}
    static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
}
    static class Var extends Stmt {
    Var(Type type, Token name, Expr initializer) {
      this.type = type;
      this.name = name;
      this.initializer = initializer;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitVarStmt(this);
    }

    final Type type;
    final Token name;
    final Expr initializer;
    Symbol symbol;
}
    static class While extends Stmt {
    While(Expr condition, Stmt body, Token keyword) {
      this.condition = condition;
      this.body = body;
      this.keyword = keyword;
}

     @Override
<R> R accept(Visitor<R> visitor) {
return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
    final Token keyword;
}

  abstract <R> R accept(Visitor<R> visitor);

}
