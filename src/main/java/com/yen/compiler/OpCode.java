package com.yen.compiler;

class OpCode {
  static final byte OP_CONSTANT = 0;
  static final byte OP_RETURN = 1;
  static final byte OP_ADD_INT = 2 ;
  static final byte OP_SUBTRACT_INT = 3;
  static final byte OP_MULTIPLY_INT = 4;
  static final byte OP_DIVIDE_INT = 5; 
  static final byte OP_ADD_DOUBLE = 6 ;
  static final byte OP_SUBTRACT_DOUBLE = 7;
  static final byte OP_MULTIPLY_DOUBLE = 8;
  static final byte OP_DIVIDE_DOUBLE = 9;
  static final byte OP_NEGATE_INT = 10;
  static final byte OP_NEGATE_DOUBLE = 11;
  static final byte OP_INT_TO_DOUBLE = 12;
  static final byte OP_GET_GLOBAL = 13;
  static final byte OP_SET_GLOBAL = 14;
  static final byte OP_GET_LOCAL = 15;
  static final byte OP_SET_LOCAL = 16;
  static final byte OP_PRINT = 17;
}
