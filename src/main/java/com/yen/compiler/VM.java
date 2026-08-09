package com.yen.compiler;
class VM {
  public enum InterpretResult {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
  }

  private static final int STACK_MAX = 256;
  private static final int GLOBALS_MAX = 256;

  private Chunk chunk;
  private int ip;
  private Object[] stack;
  int stackTop;
  private Object[] globals;

  public VM() {
    this.stack = new Object[STACK_MAX];
    this.globals = new Object[GLOBALS_MAX];
  }
  // SET/GET insruction's operand must be masked with 0xFF when read to prevent sign extention for value greater than 127.
  public InterpretResult interpret(Chunk chunk) {
    this.chunk = chunk;
    this.ip = 0;
    this.stackTop = 0;
    return run();
 }

 // since we have type-spcecific instructions  we have a better run-time performance.
 private InterpretResult run() {
   for (;;) {
     byte instruction;
     switch(instruction = readByte()) {
       case OpCode.OP_CONSTANT:
         Object value = readConstant();
         push(value);
         break;
       case OpCode.OP_INT_TO_DOUBLE:
         push((double) (int) pop());
         break;
       case OpCode.OP_RETURN:
         return InterpretResult.INTERPRET_OK;
       case OpCode.OP_POP:
         pop();
         break;
       case OpCode.OP_CONCAT:
         String b = (String) pop();
         String a = (String) pop();
         push(a + b);
         break;
       case OpCode.OP_PRINT:
         System.out.println(pop());
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
       case OpCode.OP_NOT:
         push(((boolean) pop()));
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

        
     }
   }
 }

 private byte readByte() {
   return chunk.code[ip++];
 }

 private Object readConstant() {
   int index = readByte() & 0xFF;
   return chunk.constants.get(index);
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
