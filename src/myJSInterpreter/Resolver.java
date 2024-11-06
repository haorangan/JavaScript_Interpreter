package myJSInterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Statement.Visitor<Void> {
    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
    }
    private enum ClassType {
        NONE,
        CLASS
    }
    private ClassType currentClass = ClassType.NONE;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Interpreter interpreter;
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
    @Override
    public Void visitBlockStmt(Statement.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }
    void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }
    private void resolve(Expr expr) {
        expr.accept(this);
    }
    private void resolve(Statement stmt) {
        stmt.accept(this);
    }
    @Override
    public Void visitVarStmt(Statement.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitIncDecExpr(Expr.IncDec expr) {
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitArrayExpr(Expr.Array expr) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(Statement.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }
    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }
    @Override
    public Void visitExpressionStmt(Statement.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }
    @Override
    public Void visitIfStmt(Statement.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }
    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        resolve(expr.index);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }
    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            JavaScript.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitPrintStmt(Statement.Print stmt) {
        resolve(stmt.expression);
        return null;
    }
    @Override
    public Void visitReturnStmt(Statement.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            JavaScript.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                JavaScript.error(stmt.keyword, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }
    @Override
    public Void visitClassStmt(Statement.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);
        beginScope();
        scopes.peek().put("this", true);
        for (Statement.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("constructor")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
        endScope();
        currentClass = enclosingClass;
        return null;
    }
    @Override
    public Void visitWhileStmt(Statement.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }
    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }
    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }
    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.left);
        resolve(expr.right);
        resolve(expr.first);
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            JavaScript.error(name, "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }
    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            JavaScript.error(expr.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }
    private void endScope() {
        scopes.pop();
    }
}
