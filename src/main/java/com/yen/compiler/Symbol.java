package com.yen.compiler;

class Symbol {
  final String name;
  final Type type;
  final Kind kind;
  final int scopeDepth;
  final int slotIndex;
  boolean ready;

  enum Kind { VARIABLE, PARAMETER, FUNCTION }

  Symbol (String name, Type type, Kind kind, int scopeDepth, int slotIndex) {
    this.name = name;
    this.type = type;
    this.kind = kind;
    this.scopeDepth = scopeDepth;
    this.slotIndex = slotIndex;
    this.ready = false;
  }
}
