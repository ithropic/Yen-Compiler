package com.yen.compiler;

import java.util.List;

// Debug-only: walks a RESOLVED + TYPE-CHECKED AST and prints node types.
// Run AFTER Resolver.resolveProgram() AND TypeChecker.check().
class TypeDumper implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    void dump(List<Stmt> statements) {
        for (Stmt stmt : statements) resolve(stmt);
    }

    private void resolve(Stmt stmt) { stmt.accept(this); }
    private void resolve(Expr expr) { expr.accept(this); }

    private void printType(String label, Type type) {
        System.out.println(label + " -> type=" + type);
    }

    @Override public Void visitVarStmt(Stmt.Var stmt) {
        System.out.println("VAR " + stmt.name.lexeme + " (declared=" + stmt.type + ")");
        if (stmt.initializer != null) resolve(stmt.initializer);
        return null;
    }

    @Override public Void visitFunctionStmt(Stmt.Function stmt) {
        System.out.println("FUNC " + stmt.name.lexeme + " (returns=" + stmt.type + ")");
        for (Stmt s : stmt.body) resolve(s);
        return null;
    }

    @Override public Void visitBlockStmt(Stmt.Block stmt) {
        System.out.println("{");
        for (Stmt s : stmt.statements) resolve(s);
        System.out.println("}");
        return null;
    }

    @Override public Void visitIfStmt(Stmt.If stmt) {
        System.out.println("IF");
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override public Void visitWhileStmt(Stmt.While stmt) {
        System.out.println("WHILE");
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override public Void visitReturnStmt(Stmt.Return stmt) {
        System.out.println("RETURN");
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override public Void visitPrintStmt(Stmt.Print stmt) {
        System.out.println("PRINT");
        resolve(stmt.expression);
        return null;
    }

    @Override public Void visitVariableExpr(Expr.Variable expr) {
        printType("USE " + expr.name.lexeme, expr.type);
        return null;
    }

    @Override public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        printType("ASSIGN " + expr.name.lexeme, expr.type);
        return null;
    }

    @Override public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        printType("BINARY " + expr.operator.lexeme, expr.type);
        return null;
    }

    @Override public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        printType("UNARY " + expr.operator.lexeme, expr.type);
        return null;
    }

    @Override public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        printType("LOGICAL " + expr.operator.lexeme, expr.type);
        return null;
    }

    @Override public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        printType("GROUP", expr.type);
        return null;
    }

    @Override public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr arg : expr.arguments) resolve(arg);
        printType("CALL " + expr.callee.name.lexeme, expr.type);
        return null;
    }

    @Override public Void visitLiteralExpr(Expr.Literal expr) {
        printType("LITERAL " + expr.value, expr.type);
        return null;
    }
}
