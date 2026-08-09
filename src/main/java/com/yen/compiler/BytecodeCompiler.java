package com.yen.compiler;

import java.util.List;

class BytecodeCompiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private Chunk chunk;

  Chunk compile(List<Stmt> statements) {
    chunk = new Chunk();

    for (Stmt stmt : statements) {
      compile(stmt);
    }
    emitByte(OpCode.OP_RETURN, 0); // temporarily, real end of program will be handled later.
    return chunk;
  }

  private void compile(Expr expr) { expr.accept(this); }
  private void compile(Stmt stmt) { stmt.accept(this); }

  private void emitByte(byte b, int line) {
    chunk.writeByte(b, line);
  }

  private void emitBytes(byte b1, byte b2, int line) {
    chunk.writeByte(b1, line);
    chunk.writeByte(b2, line);
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    chunk.writeConstant(expr.value, expr.line);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    compile(expr.left);

    if (expr.left.type == Type.INT && expr.type == Type.DOUBLE) {
      emitByte(OpCode.OP_INT_TO_DOUBLE, expr.operator.line);
    }
    compile(expr.right);

    if (expr.right.type == Type.INT && expr.type == Type.DOUBLE) {
      emitByte(OpCode.OP_INT_TO_DOUBLE, expr.operator.line);
    }

    emitByte(pickOpcode(expr.operator, expr.type), expr.operator.line);

    return null;
  }

  private byte pickOpcode(Token operator, Type type) {
    TokenType opType = operator.type;
    if (type == Type.INT) {
      switch (opType) {
      case PLUS:
        return OpCode.OP_ADD_INT;
      case MINUS:
        return OpCode.OP_SUBTRACT_INT;
      case STAR:
        return OpCode.OP_MULTIPLY_INT;
      case SLASH:
        return OpCode.OP_DIVIDE_INT;
      default:
        throw new IllegalArgumentException("Unknown binary operator type: " + opType);
      }
    }
    else if (type == Type.DOUBLE) {
      switch (opType) {
      case PLUS:
        return OpCode.OP_ADD_DOUBLE;
      case MINUS:
        return OpCode.OP_SUBTRACT_DOUBLE;
      case STAR:
        return OpCode.OP_MULTIPLY_DOUBLE;
      case SLASH:
        return OpCode.OP_DIVIDE_DOUBLE; 
      default:
        throw new IllegalArgumentException("Unknown binary operator type: " + opType);

      }
    } else if (type == Type.STRING && opType == TokenType.PLUS) {
        return OpCode.OP_CONCAT;
      } else {
          throw new IllegalArgumentException("Unsupported operand type for binary operator: " + type);
        }
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    compile(expr.expression);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    compile(expr.right);
    emitByte(pickUnaryOpCode(expr.operator, expr.right.type), expr.operator.line);
    return null;
  }

  private byte pickUnaryOpCode(Token op, Type type) {
    if (op.type == TokenType.BANG) return OpCode.OP_NOT;
    if (op.type == TokenType.MINUS) {
      if (type == Type.INT) {
        return OpCode.OP_NEGATE_INT;
      } else if (type == Type.DOUBLE) {
        return OpCode.OP_NEGATE_DOUBLE;
      } else {
       throw new IllegalArgumentException("Cannot negate type: " + type);
      }
    } else {
       throw new IllegalArgumentException("Unknown unary operator type: " + op.type);
    }
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (expr.symbol.scopeDepth == 0) {
      emitBytes(OpCode.OP_GET_GLOBAL, toByte(expr.symbol.slotIndex, "slot"), expr.name.line);
    } else {
      emitBytes(OpCode.OP_GET_LOCAL, toByte(expr.symbol.slotIndex, "slot"), expr.name.line);
    }
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    compile(expr.value);
    if (expr.value.type == Type.INT && expr.type == Type.DOUBLE) {
      emitByte(OpCode.OP_INT_TO_DOUBLE, expr.name.line);
    }
    if (expr.symbol.scopeDepth == 0) {
      emitBytes(OpCode.OP_SET_GLOBAL, toByte(expr.symbol.slotIndex, "slot"), expr.name.line);
    } else {
      emitBytes(OpCode.OP_SET_LOCAL, toByte(expr.symbol.slotIndex, "slot"), expr.name.line);
    }
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer == null) {
      chunk.writeConstant(zeroValue(stmt.type), stmt.name.line);
    } else {
      compile(stmt.initializer);

      if (stmt.initializer.type == Type.INT && stmt.type == Type.DOUBLE) {
        emitByte(OpCode.OP_INT_TO_DOUBLE, stmt.name.line);
      }
    }

    if (stmt.symbol.scopeDepth == 0) {
      emitBytes(OpCode.OP_DEFINE_GLOBAL, toByte(stmt.symbol.slotIndex, "slot"), stmt.name.line);
    } else {
      emitBytes(OpCode.OP_DEFINE_LOCAL, toByte(stmt.symbol.slotIndex, "slot"), stmt.name.line);
    }
    return null;
  }

  private byte toByte(int index, String whichIndex) {
    if (index < 0 || index > 255) {
      throw new IllegalStateException(whichIndex + " index " + index + " exceeds byte range (max 255).");
    }

    return (byte) index;
  }

  private Object zeroValue(Type type) {
    switch (type) {
      case INT: return 0;
      case DOUBLE: return 0.0;
      case BOOL: return false;
      case STRING: return "";
      default: throw new IllegalArgumentException("No default initializer for type: " + type);
    }
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    compile(stmt.expression);
    emitByte(OpCode.OP_POP, stmt.line);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    compile(stmt.expression);
    emitByte(OpCode.OP_PRINT, stmt.line);
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    throw new UnsupportedOperationException("Not yet implemented.");

  }
  
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    throw new UnsupportedOperationException("Not yet implemented.");

  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    throw new UnsupportedOperationException("Not yet implemented.");

  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }
  


}
