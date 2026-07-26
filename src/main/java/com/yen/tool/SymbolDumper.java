package com.yen.compiler;

import java.util.List;

// Debug-only: walks a RESOLVED AST and prints what got attached.
// Run this AFTER Resolver.resolveProgram(), not instead of it.
class SymbolDumper implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    void dump(List<Stmt> statements) {
        for (Stmt stmt : statements) resolve(stmt);
    }

    private void resolve(Stmt stmt) { stmt.accept(this); }
    private void resolve(Expr expr) { expr.accept(this); }

    private void printSymbol(String label, Symbol sym) {
        if (sym == null) {
            System.out.println(label + " -> NULL SYMBOL");
            return;
        }
        System.out.printf("%s -> name=%s kind=%s slot=%d depth=%d type=%s ready=%b%n",
            label, sym.name, sym.kind, sym.slotIndex, sym.scopeDepth, sym.type, sym.ready);
    }

    @Override public Void visitVarStmt(Stmt.Var stmt) {
        printSymbol("VAR " + stmt.name.lexeme, stmt.symbol);
        if (stmt.initializer != null) resolve(stmt.initializer);
        return null;
    }

    @Override public Void visitFunctionStmt(Stmt.Function stmt) {
        printSymbol("FUNC " + stmt.name.lexeme, stmt.symbol);
        for (Stmt s : stmt.body) resolve(s);
        return null;
    }

    @Override public Void visitBlockStmt(Stmt.Block stmt) {
        System.out.println("{");
        for (Stmt s : stmt.statements) resolve(s);
        System.out.println("}");
        return null;
    }

    @Override public Void visitVariableExpr(Expr.Variable expr) {
        printSymbol("USE " + expr.name.lexeme, expr.symbol);
        return null;
    }

    @Override public Void visitAssignExpr(Expr.Assign expr) {
        printSymbol("ASSIGN " + expr.name.lexeme, expr.symbol);
        resolve(expr.value);
        return null;
    }

    @Override public Void visitBinaryExpr(Expr.Binary expr) { resolve(expr.left); resolve(expr.right); return null; }
    @Override public Void visitUnaryExpr(Expr.Unary expr) { resolve(expr.right); return null; }
    @Override public Void visitLogicalExpr(Expr.Logical expr) { resolve(expr.left); resolve(expr.right); return null; }
    @Override public Void visitGroupingExpr(Expr.Grouping expr) { resolve(expr.expression); return null; }
    @Override public Void visitCallExpr(Expr.Call expr) { resolve(expr.callee); for (Expr a : expr.arguments) resolve(a); return null; }
    @Override public Void visitLiteralExpr(Expr.Literal expr) { return null; }
    @Override public Void visitExpressionStmt(Stmt.Expression stmt) { resolve(stmt.expression); return null; }
    @Override public Void visitPrintStmt(Stmt.Print stmt) { resolve(stmt.expression); return null; }
    @Override public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition); resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }
    @Override public Void visitReturnStmt(Stmt.Return stmt) { if (stmt.value != null) resolve(stmt.value); return null; }
    @Override public Void visitWhileStmt(Stmt.While stmt) { resolve(stmt.condition); resolve(stmt.body); return null; }
}
