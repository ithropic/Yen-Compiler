package com.yen.compiler;

import java.util.List;

class Symbol {
  final String name;
  final Type type;
  final Kind kind;
  final int scopeDepth;
  final int slotIndex;
  boolean ready;
  List<Type> paramTypes; // only used if Kind == FUNCTION; null otherwise.

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
