package com.yen.compiler;

import java.util.ArrayDeque;

class VM {
  public enum InterpretResult {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
  }

  private final ArrayDeque<CallFrame> callFrameStack = new ArrayDeque<>();
  private CallFrame frame;

  private static final int STACK_MAX = 256;
  private static final int GLOBALS_MAX = 256;

  private Object[] stack;
  int stackTop;
  private Object[] globals;

  public VM() {
    this.stack = new Object[STACK_MAX];
    this.globals = new Object[GLOBALS_MAX];
  }
  // SET/GET insruction's operand must be masked with 0xFF when read to prevent sign extention for value greater than 127.
  public InterpretResult interpret(Chunk chunk) {
    callFrameStack.push(new CallFrame(chunk, 0, 0));
    return run();
 }

 // since we have type-spcecific instructions  we have a better run-time performance.
 private InterpretResult run() {
   while (!callFrameStack.isEmpty()) {
     frame = callFrameStack.peek();
     byte instruction;
     switch(instruction = readByte()) {
       case OpCode.OP_CONSTANT:
         Object value = readConstant();
         push(value);
         break;
       case OpCode.OP_RETURN_VOID: {
         stackTop = frame.frameBase - 1;
         callFrameStack.pop();
         if (callFrameStack.isEmpty()) {
          return InterpretResult.INTERPRET_OK;
         } 
         break; }
       case OpCode.OP_RETURN_VALUE: {
         Object returnValue = pop();
         stackTop = frame.frameBase - 1;
         push(returnValue);
         callFrameStack.pop();
         if (callFrameStack.isEmpty()) {
          return InterpretResult.INTERPRET_OK;
         } 
         break; }
       case OpCode.OP_INT_TO_DOUBLE:
         push((double) (int) pop());
         break;
       case OpCode.OP_PRINT:
         System.out.println(pop());
         break;
       case OpCode.OP_POP:
         pop();
         break;
     case OpCode.OP_CONCAT: {
         String b = (String) pop();
         String a = (String) pop();
         push(a + b);
         break;
       }
     case OpCode.OP_CALL:
       int argc = readByte() & 0xFF;
       int frameBase = stackTop - argc;
       YenFunction function = (YenFunction) stack[frameBase - 1];
       callFrameStack.push(new CallFrame(function.chunk, 0, frameBase));
       break;
      case OpCode.OP_NEGATE_INT:
         push(- (int)pop());
         break;
       case OpCode.OP_ADD_INT:
         binaryOp_INT('+');
         break;
       case OpCode.OP_SUBTRACT_INT:
         binaryOp_INT('-');
         break;
       case OpCode.OP_MULTIPLY_INT:
         binaryOp_INT('*');
         break;
       case OpCode.OP_DIVIDE_INT:
         binaryOp_INT('/');
         break; 
       case OpCode.OP_NEGATE_DOUBLE:
         push(- (double)pop());
         break;
       case OpCode.OP_ADD_DOUBLE:
         binaryOp_DOUBLE('+');
         break;
       case OpCode.OP_SUBTRACT_DOUBLE:
         binaryOp_DOUBLE('-');
         break;
       case OpCode.OP_MULTIPLY_DOUBLE:
         binaryOp_DOUBLE('*');
         break;
       case OpCode.OP_DIVIDE_DOUBLE:
         binaryOp_DOUBLE('/');
         break;
       case OpCode.OP_EQUAL_EQUAL:{
        Object b = pop();
        Object a = pop();
        if (a instanceof Number && b instanceof Number) {
          push(((Number)a).doubleValue() == (((Number)b).doubleValue()));
        } else {
          push((a.equals(b)));
        }
        break;
       }
       case OpCode.OP_BANG_EQUAL:{
        Object b = pop();
        Object a = pop();
        if (a instanceof Number && b instanceof Number) {
          push(((Number)a).doubleValue() != (((Number)b).doubleValue()));
        } else {
          push(!(a.equals(b)));
        }
        break;
       }
       case OpCode.OP_LESS: {
        double b = ((Number)pop()).doubleValue();
        double a = ((Number)pop()).doubleValue();
        push(a < b);
        break;
       }
       case OpCode.OP_LESS_EQUAL: {
        double b = ((Number)pop()).doubleValue();
        double a = ((Number)pop()).doubleValue();
        push(a <= b);
        break;
       }
       case OpCode.OP_GREATER: {
        double b = ((Number)pop()).doubleValue();
        double a = ((Number)pop()).doubleValue();
        push(a > b);
        break;
       }
       case OpCode.OP_GREATER_EQUAL: {
        double b = ((Number)pop()).doubleValue();
        double a = ((Number)pop()).doubleValue();
        push(a >= b);
        break;
       }
       case OpCode.OP_NOT:
         push(!((boolean) pop()));
         break;
       case OpCode.OP_DEFINE_GLOBAL: {
         int slot = readByte() & 0xFF;
         globals[slot] = pop();
         break;
       }
       case OpCode.OP_GET_GLOBAL: {
         int slot = readByte() & 0xFF;
         push(globals[slot]);
         break;
       }
       case OpCode.OP_SET_GLOBAL: {
         int slot = readByte() & 0xFF;
         globals[slot] = peek();
         break; 
       }
       case OpCode.OP_DEFINE_LOCAL: {
        readByte(); // read the operand  byte.
        break; // we have nothing to do, since locals sit directly on the stack
               // the initializer value will be naturally sitting on the right slot index.
       }
       case OpCode.OP_GET_LOCAL: {
         int slot = readByte() & 0xFF;
         push(stack[frame.frameBase + slot]);
         break;
       }
       case OpCode.OP_SET_LOCAL: {
         int slot = readByte() & 0xFF;
         stack[frame.frameBase + slot] = peek();
         break;
       }
       case OpCode.OP_JUMP: {
        int offset = readOffset();
          frame.ip += offset;
        break;
       }
       case OpCode.OP_JUMP_IF_FALSE: {
        int offset = readOffset();
        if (!((boolean) peek())) frame.ip += offset;
        break;
       }
       case OpCode.OP_LOOP: {
        int distance = readOffset();
        frame.ip -= distance;
        break;
       }

     }
   }
   return InterpretResult.INTERPRET_OK;
 }

 private byte readByte() {
   return frame.chunk.code[frame.ip++];
 }

 private Object readConstant() {
   int index = readByte() & 0xFF;
   return frame.chunk.constants.get(index);
 }

 void push(Object value) {
   stack[stackTop++] = value;
 } 

 Object pop() {
   if (stackTop == 0) return null;
   return stack[--stackTop];
 }

 Object peek() {
   if (stackTop == 0) return null;
   return stack[stackTop - 1];
 }

 int readOffset() {
   int high = readByte() & 0xFF;
   high = high << 8;
   int low = readByte() & 0xFF;
   return high | low;
 }

 void binaryOp_INT(char op) {
   int b = (int)pop();
   int a = (int)pop();
   switch(op) {
   case '+': push(a + b); break;
   case '-': push(a - b); break;
   case '*': push(a * b); break;
   case '/': push(a / b); break;
   }
 }

  void binaryOp_DOUBLE(char op) {
    double b = (double)pop();
    double a = (double)pop();
    switch(op) {
    case '+': push(a + b); break;
    case '-': push(a - b); break;
    case '*': push(a * b); break;
    case '/': push(a / b); break;
    }
  }


}
