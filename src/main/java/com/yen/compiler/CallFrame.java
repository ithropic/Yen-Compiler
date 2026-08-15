package com.yen.compiler;

class CallFrame {
  Chunk chunk;
  int ip;
  int frameBase;

  CallFrame(Chunk chunk, int ip, int frameBase) {
    this.chunk = chunk;
    this.ip = ip;
    this.frameBase = frameBase;
  }
}
