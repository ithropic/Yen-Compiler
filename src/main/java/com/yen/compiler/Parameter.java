package com.yen.compiler;


// Function parameters have a Type and a name
// so we store each paremeter as a record of 
// these two fields in the parameter array of 
// the parsed function declaration.
record Parameter(Type type, Token name) {}
