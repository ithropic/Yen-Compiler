package com.yen.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  // Deque stands for double-ended-queue which we'll use as a fancy stack.
  private final ArrayDeque<Map<String, Symbol>> scopes = new ArrayDeque<>();

  // we'll assign each variable to an integer index slot.
  // so we can completely get rid of varibale names and start
  // accessing them in an array using their indexes.
  // when we enter a function call a new call frame is pushed onto the call stack,
  // with its own new locals array, paramenters values,
  // and return address(instruction pointer to get back after the call)
  // inside the fucntion each time a variable is declared in a block
  // we reserve the current free slot in the locals array. when we exit
  // the block the array shrinks and the slot is free to be used again.

  // global single map.
  private final Map<String, Symbol> globals = new HashMap<>();
  private int nextGlobalSlot = 0;

  // stack of maps used to assign each variable in a current scope
  // to a slot number relative to the current scope.
  private int nextLocalSlot = 0;
  private final ArrayDeque<Integer> slotCheckPoints = new ArrayDeque<>();

  void resolveProgram(List<Stmt> statements) {
    // first pass to register function and global variables declarations.
    for (Stmt statement : statements) {
      if (statement instanceof Stmt.Function f) {
        Symbol sym = declare(f.name, f.type, Symbol.Kind.FUNCTION);
        define(f.name.lexeme);
        f.symbol = sym;
      } else if (statement instanceof Stmt.Var v) {
        Symbol sym = declare(v.name, v.type, Symbol.Kind.VARIABLE);
        define(v.name.lexeme); // forward reference support for globals
        // at the cost of losing self initialization, accepted as a trade off.
        v.symbol = sym;
      }
    }
    // second pass.
    resolve(statements);
  }

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  private void resolve(Stmt statement) {
    statement.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
    slotCheckPoints.push(nextLocalSlot);
  }

  private void endScope() {
    scopes.pop();
    nextLocalSlot = slotCheckPoints.pop();
  }

  private Symbol declare(Token name, Type type, Symbol.Kind kind) {
    if (scopes.isEmpty()) { // global scope.
      if (globals.containsKey(name.lexeme)) {
        error(name, "Global variable with this name already exist.");
      }
      Symbol sym = new Symbol(name.lexeme, type, kind, 0, nextGlobalSlot++);
      globals.put(name.lexeme, sym);
      return sym;
    } else {
      Map<String, Symbol> scope = scopes.peek(); // current scope.
      if (scope.containsKey(name.lexeme)) {
        error(name, "Variable with this name already exist in this scope.");
      }
      Symbol sym = new Symbol(name.lexeme, type, kind, scopes.size(), nextLocalSlot++);
      scope.put(name.lexeme, sym);
      return sym;
    }
  }

  private void define(String name) {
    if (scopes.isEmpty()) {
      globals.get(name).ready = true;
    } else {
      scopes.peek().get(name).ready = true;
    }
  }

  private Symbol resolve(Token name) {
    // starting from the top of the stack (the innermost scope).
    for (Map<String, Symbol> scope : scopes) {
      Symbol sym = scope.get(name.lexeme);
      if (sym != null)
        return sym;
    }
    Symbol sym = globals.get(name.lexeme);
    if (sym != null)
      return sym;

    error(name, "Undefined identifier '" + name.lexeme + "'.");
    return null;
  }

  private void error(Token token, String message) {
    Compiler.error(token, message);
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Symbol sym;
    if (scopes.isEmpty()) {
      // we already declared it during the first pass "hoisting".
      sym = globals.get(stmt.name.lexeme);
      if (stmt.initializer != null) {
        resolve(stmt.initializer);
      }
    } else {
      sym = declare(stmt.name, stmt.type, Symbol.Kind.VARIABLE);
      if (stmt.initializer != null) {
        resolve(stmt.initializer);
      }
      define(stmt.name.lexeme);
    }

    stmt.symbol = sym;
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    if (!scopes.isEmpty()) {
      error(stmt.name, "Functions can only be declared at global scope.");
      return null;
    }
    Symbol sym = globals.get(stmt.name.lexeme); // already registred on the first pass.

    List<Type> paramTypes = new ArrayList<>();
    for (Parameter p : stmt.params) {
      paramTypes.add(p.type());
    }
    sym.paramTypes = paramTypes;

    stmt.symbol = sym;

    int savedNextLocalSlot = nextLocalSlot;
    nextLocalSlot = 0; // because we're entering a function body
    // and functions have their own locals.
    beginScope();

    for (Parameter param : stmt.params) { // declaring parameters as the firs local variables.
      declare(param.name(), param.type(), Symbol.Kind.PARAMETER);
      define(param.name().lexeme);
    }
    resolve(stmt.body);

    endScope();
    nextLocalSlot = savedNextLocalSlot;
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    Symbol sym = resolve(expr.name);
    if (sym != null) {
      if (!sym.ready) {
        error(expr.name, "Can't use variable in its own initializer.");
      }
      expr.symbol = sym;
      expr.type = sym.type;
    }
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    Symbol sym = resolve(expr.name);
    if (sym != null) {
      expr.symbol = sym;
      expr.type = sym.type;
    }
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);
    for (Expr arg : expr.arguments) {
      resolve(arg);
    }
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) {
      resolve(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (stmt.value != null) {
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

}
