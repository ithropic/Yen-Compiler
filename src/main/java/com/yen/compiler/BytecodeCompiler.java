package com.yen.compiler;

import java.util.ArrayDeque;
import java.util.List;

class BytecodeCompiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private Chunk chunk;
  private final ArrayDeque<Integer> localCountStack = new ArrayDeque<>();

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
      } else if (type == Type.BOOL) {
        switch(opType) {
          case AND:
            return OpCode.OP_AND;
          case OR:
            return OpCode.OP_OR;
          case EQUAL_EQUAL:
            return OpCode.OP_EQUAL_EQUAL;
          case BANG_EQUAL:
            return OpCode.BANG_EQUAL;
          case LESS:
            return OpCode.OP_LESS;
            case LESS_EQUAL:
            return OpCode.OP_LESS_EQUAL;
            case GREATER:
            return OpCode.OP_GREATER;
            case GREATER_EQUAL:
            return OpCode.OP_GREATER_EQUAL;
        }
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
      localCountStack.push(localCountStack.pop() + 1);
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
    if (expr.operator.type == TokenType.AND) {
      compile(expr.left);
      emitByte(OpCode.OP_JUMP_IF_FALSE, expr.operator.line); // short-circuit.
      emitBytes((byte) 0xFF, (byte) 0xFF, expr.operator.line);
      int offset = chunk.count - 2;
      
      emitByte(OpCode.OP_POP, expr.operator.line);

      compile(expr.right);
      int jumpDistance = chunk.count - offset - 2;
      chunk.patchByte(offset, (byte) ((jumpDistance >> 8) & 0xFF));
      chunk.patchByte(offset + 1, (byte) (jumpDistance & 0xFF));
    } else if (expr.operator.type == TokenType.OR) {
      compile(expr.left);

      emitByte(OpCode.OP_JUMP_IF_FALSE, expr.operator.line);
      emitBytes((byte) 0xFF, (byte) 0xFF, expr.operator.line);
      int falseOffset = chunk.count - 2;

      emitByte(OpCode.OP_JUMP, expr.operator.line);
      emitBytes((byte) 0xFF, (byte) 0xFF, expr.operator.line);
      int offset = chunk.count - 2;

      emitByte(OpCode.OP_POP, expr.operator.line);

      int falseJumpDistance = chunk.count - falseOffset - 2;
      chunk.patchByte(falseOffset, (byte) ((falseJumpDistance >> 8) & 0xFF));
      chunk.patchByte(falseOffset + 1, (byte) (falseJumpDistance & 0xFF));


      compile(expr.right);
      int jumpDistance = chunk.count - offset - 2;
      chunk.patchByte(offset, (byte) ((jumpDistance >> 8) & 0xFF));
      chunk.patchByte(offset + 1, (byte) (jumpDistance & 0xFF));
    } else {
       throw new IllegalArgumentException("Unknown logical operator: " + expr.operator.type);
    }
    
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    localCountStack.push(0);
    for (Stmt statement : stmt.statements) {
      compile(statement);
    }
    int count = localCountStack.pop();
    for (int i = 0; i < count; i++) {
      emitByte(OpCode.OP_POP, stmt.exitBraceLine);
    }
    return null;
  }
  
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    compile(stmt.condition);
    emitByte(OpCode.OP_JUMP_IF_FALSE, stmt.keyword.line);
    emitBytes((byte) 0xFF, (byte) 0xFF, stmt.keyword.line); // place holder bytes (big endian).
    int jumpOffset = chunk.count - 2; // points to the high byte of the offset.

    emitByte(OpCode.OP_POP, stmt.keyword.line);
    compile(stmt.thenBranch);

    int elseJumpOffset = 0;
    if (stmt.elseBranch != null) {
    emitByte(OpCode.OP_JUMP, stmt.keyword.line);
    emitBytes((byte) 0xFF, (byte) 0xFF, stmt.keyword.line);
    elseJumpOffset = chunk.count - 2;
    }


    int jumpDistance = chunk.count - jumpOffset - 2;
    chunk.patchByte(jumpOffset, (byte) ((jumpDistance >> 8) & 0xFF)); // update the JUMP_IF_FALSE offset 
                                                              // to jump after the if block and the unconditional JUMP
                                                              // directly inside the else block after we know its size.
    chunk.patchByte(jumpOffset + 1, (byte) ((jumpDistance) & 0xFF));

    emitByte(OpCode.OP_POP, stmt.keyword.line);

    if (stmt.elseBranch != null) {
    compile(stmt.elseBranch);
    jumpDistance = chunk.count - elseJumpOffset - 2; // update the unconditional JUMP offset
                                                     // to jump past the else block if the condition is true.
                                                     // this jump will be executed directly after the if block
                                                     // if the condition is true and we jump past it
                                                     // to execute the else block if the condition is false.
    chunk.patchByte(elseJumpOffset, (byte) ((jumpDistance >> 8) & 0xFF));
    chunk.patchByte(elseJumpOffset + 1, (byte) ((jumpDistance) & 0xFF)); 
    }

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    int loopStart = chunk.count;
    compile(stmt.condition);
    emitByte(OpCode.OP_JUMP_IF_FALSE, stmt.keyword.line);
    emitBytes((byte) 0xFF, (byte) 0xFF, stmt.keyword.line);
    int offset = chunk.count - 2;

    emitByte(OpCode.OP_POP, stmt.keyword.line);

    compile(stmt.body);


    emitByte(OpCode.OP_LOOP, stmt.keyword.line);
    int loopDistance = chunk.count - loopStart + 2; // +2 because you need to take into consideration the two bytes emitted after.
    emitBytes((byte) (((loopDistance) >> 8) & 0xFF), (byte) ((loopDistance) & 0xFF), stmt.keyword.line);

    int jumpDistance = chunk.count - offset - 2;
    chunk.patchByte(offset, (byte) ((jumpDistance >> 8) & 0xFF));
    chunk.patchByte(offset + 1, (byte) (jumpDistance & 0xFF));

    emitByte(OpCode.OP_POP, stmt.keyword.line);

    return null;
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
