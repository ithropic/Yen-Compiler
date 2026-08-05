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

      default:
        System.out.println("Unknown opcode " + instruction);
        return offset + 1;
    }
  }

  private static int constantInstruction(String opcode, Chunk chunk, int offset) {
    int index = chunk.code[offset + 1] & 0xFF; // java's byte is signe (-128, 127) when this byte is extended to 
                                               // a 32 bits int used to index the code array, the sign is preserved
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
}
