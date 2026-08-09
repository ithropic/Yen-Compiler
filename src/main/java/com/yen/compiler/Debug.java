package com.yen.compiler;

class Debug {
   static void disassembleChunk(Chunk chunk, String name) {
    System.out.printf("== %s ==\n", name);

    for (int offset = 0; offset < chunk.count; ) {
      offset = disassembleInstruction(chunk, offset);
    }
  }

   static int disassembleInstruction(Chunk chunk, int offset) {
    System.out.printf("%04d ", offset);

    if (offset > 0 && chunk.lines[offset] == chunk.lines[offset - 1]) {
      System.out.printf("   | ");
    } else {
      System.out.printf("%4d ", chunk.lines[offset]);
    }

    byte instruction = chunk.code[offset];
    switch (instruction) {
      case OpCode.OP_CONSTANT:
        return constantInstruction("OP_CONSTANT", chunk, offset);
      case OpCode.OP_RETURN:
        return simpleInstruction("OP_RETURN", chunk, offset);
            case OpCode.OP_POP:
        return simpleInstruction("OP_POP", chunk, offset);
      case OpCode.OP_NOT:
        return simpleInstruction("OP_NOT", chunk, offset);
      case OpCode.OP_INT_TO_DOUBLE:
        return simpleInstruction("OP_INT_TO_DOUBLE", chunk, offset);
      case OpCode.OP_CONCAT:
        return simpleInstruction("OP_CONCAT", chunk, offset);
      case OpCode.OP_PRINT:
        return simpleInstruction("OP_PRINT", chunk, offset);
      case OpCode.OP_ADD_INT:
        return simpleInstruction("OP_ADD_INT", chunk, offset);
      case OpCode.OP_SUBTRACT_INT:
        return simpleInstruction("OP_SUBTRACT_INT", chunk, offset);
      case OpCode.OP_MULTIPLY_INT:
        return simpleInstruction("OP_MULTIPLY_INT", chunk, offset);
      case OpCode.OP_DIVIDE_INT:
        return simpleInstruction("OP_DIVIDE_INT", chunk, offset);
      case OpCode.OP_NEGATE_INT:
        return simpleInstruction("OP_NEGATE_INT", chunk, offset);

      case OpCode.OP_ADD_DOUBLE:
        return simpleInstruction("OP_ADD_DOUBLE", chunk, offset);
      case OpCode.OP_SUBTRACT_DOUBLE:
        return simpleInstruction("OP_SUBTRACT_DOUBLE", chunk, offset);
      case OpCode.OP_MULTIPLY_DOUBLE:
        return simpleInstruction("OP_MULTIPLY_DOUBLE", chunk, offset);
      case OpCode.OP_DIVIDE_DOUBLE:
        return simpleInstruction("OP_DIVIDE_DOUBLE", chunk, offset);
      case OpCode.OP_NEGATE_DOUBLE:
        return simpleInstruction("OP_NEGATE_DOUBLE", chunk, offset);

      case OpCode.OP_DEFINE_GLOBAL:
        return byteInstruction("OP_DEFINE_GLOBAL", chunk, offset);
      case OpCode.OP_GET_GLOBAL:
        return byteInstruction("OP_GET_GLOBAL", chunk, offset);
      case OpCode.OP_SET_GLOBAL:
        return byteInstruction("OP_SET_GLOBAL", chunk, offset);
      case OpCode.OP_DEFINE_LOCAL:
        return byteInstruction("OP_DEFINE_LOCAL", chunk, offset);
      case OpCode.OP_GET_LOCAL:
        return byteInstruction("OP_GET_LOCAL", chunk, offset);
      case OpCode.OP_SET_LOCAL:
        return byteInstruction("OP_SET_LOCAL", chunk, offset);

      default:
        System.out.println("Unknown opcode " + instruction);
        return offset + 1;
    }
  }

  private static int constantInstruction(String opcode, Chunk chunk, int offset) { 
    int index = chunk.code[offset + 1] & 0xFF; // java's byte is signe (-128, 127) when this byte is extended to a 32 bits int used to index the code array, the sign is preserved
                                               // with sign extension, to avoid this we use bitwise logical and to 
                                               // preserve the first 8 bits and zero-out the rest 24 bits.
    Object value = chunk.constants.get(index);
    System.out.println(opcode + " " + value);
    return offset + 2;
  }

  private static int simpleInstruction(String opcode, Chunk chunk, int offset) {
    System.out.printf("%s\n", opcode);
    return offset + 1;
  }

  private static int byteInstruction(String opcode, Chunk chunk, int offset) {
    int slot = chunk.code[offset + 1] & 0xFF;
    System.out.println(opcode + " " + slot);
    return offset + 2;
  }
}
