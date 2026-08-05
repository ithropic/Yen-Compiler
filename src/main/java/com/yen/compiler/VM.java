package com.yen.compiler;
class VM {
  public enum InterpretResult {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
  }

  private static final int STACK_MAX = 256;

  private Chunk chunk;
  private int ip;
  private Object[] stack;
  int stackTop;

  public VM() {
    this.stack = new Object[STACK_MAX];
  }

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

       case OpCode.OP_RETURN:
         return InterpretResult.INTERPRET_OK;
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
   return stack[--stackTop];
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
