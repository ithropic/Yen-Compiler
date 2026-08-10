package com.yen.compiler;

import java.util.ArrayList;
import java.util.List;

class Chunk {
  byte[] code = new byte[8];
  int count = 0;
  int capacity = 8;

  int[] lines = new int[8];

  List<Object> constants = new ArrayList<>();

  void writeByte(byte b, int line) {
    if (count == capacity) {
      capacity *= 2;
      code = java.util.Arrays.copyOf(code, capacity);
      lines = java.util.Arrays.copyOf(lines, capacity);
    }
    code[count] = b;
    lines[count] = line;
    count++;
  }

  int addConstant(Object value) {
    constants.add(value);
    return constants.size() - 1;
  }

  void writeConstant(Object value, int line) {
    int index = addConstant(value);
    writeByte(OpCode.OP_CONSTANT, line);
    writeByte((byte) index, line);
  }

  void patchByte(int offset, byte value) {
    code[offset] = value;
  }
  
}
