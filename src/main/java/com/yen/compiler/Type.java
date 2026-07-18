package com.yen.compiler;

enum Type {
  INT,
  DOUBLE,
  VOID, // function return type.
  BOOL,
  STIRNG,
  ERROR // sentinel used when type checking fails on a node
        // to avoid cascading errors.
}

