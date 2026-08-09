package com.yen.compiler;

class OpCode {

  // core.
  static final byte OP_CONSTANT = 0x00; static final byte OP_RETURN = 0x01;
  static final byte OP_INT_TO_DOUBLE = 0x02;
  static final byte OP_PRINT = 0x03;
  static final byte OP_POP = 0x04;
  static final byte OP_CONCAT = 0x05;


  // arithmetic INT.
  static final byte OP_ADD_INT = 0x10 ;
  static final byte OP_SUBTRACT_INT = 0x11;
  static final byte OP_MULTIPLY_INT = 0x12;
  static final byte OP_DIVIDE_INT = 0x13; 
  static final byte OP_NEGATE_INT = 0x14;


  // arithmetic DOUBLE.
  static final byte OP_ADD_DOUBLE = 0x20 ;
  static final byte OP_SUBTRACT_DOUBLE = 0x21;
  static final byte OP_MULTIPLY_DOUBLE = 0x22;
  static final byte OP_DIVIDE_DOUBLE = 0x23;
  static final byte OP_NEGATE_DOUBLE = 0x24;


  // logical.
  static final byte OP_NOT = 0x30;

  // variable.
  static final byte OP_DEFINE_GLOBAL = 0x40;
  static final byte OP_GET_GLOBAL = 0x41;
  static final byte OP_SET_GLOBAL = 0x42;
  static final byte OP_DEFINE_LOCAL = 0x43;
  static final byte OP_GET_LOCAL = 0x44;
  static final byte OP_SET_LOCAL = 0x45;
}
