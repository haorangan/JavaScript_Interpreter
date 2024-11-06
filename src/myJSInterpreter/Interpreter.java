package myJSInterpreter;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Statement.Visitor<Object> {
    final Environment globalEnv = new Environment();
    private Environment currentEnv = globalEnv;
    private final Map<Expr, Integer> locals = new HashMap<>();
    Interpreter() {
        globalEnv.define("clock", new JSCallable() {
            @Override
            public int arity() { return 0; }
            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            JavaScript.runtimeError(error);
        }
    }
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch(expr.operator.type) {
            case MINUS:
                return (double) left - (double) right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    String text = ((double) left + (double) right) + "";
                    if (text.endsWith(".0")) {
                        text = text.substring(0, text.length() - 2);
                    }
                    return Double.parseDouble(text);
                }
                if(left instanceof String || right instanceof String) {
                    if (left instanceof Double) {
                        String text = left.toString();
                        if (text.endsWith(".0")) {
                            text = text.substring(0, text.length() - 2);
                        }
                        return text + right;
                    }
                    if (right instanceof Double) {
                        String text = right.toString();
                        if (text.endsWith(".0")) {
                            text = text.substring(0, text.length() - 2);
                        }
                        return left + text;
                    }
                    return left.toString() + right.toString();
                }
                throw new RuntimeError(expr.operator, "can't add these");
            case SLASH:
                checkIsNumbers(expr.operator, left, right);
                if(Double.parseDouble(right.toString()) == 0) {
                    throw new RuntimeError(expr.operator, "can't divide by zero");
                }
                return (double) left / (double) right;
            case STAR:
                checkIsNumbers(expr.operator, left, right);
                return (double) left * (double) right;
            case MOD:
                checkIsNumbers(expr.operator, left, right);
                if(Double.parseDouble(right.toString()) == 0) {
                    throw new RuntimeError(expr.operator, "can't divide by zero");
                }
                return (double) left % (double) right;
            case GREATER:
                checkIsNumbers(expr.operator, left, right);
                return (double) left > (double) right;
            case LESS:
                checkIsNumbers(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkIsNumbers(expr.operator, left, right);
                return (double) left <= (double) right;
            case GREATER_EQUAL:
                checkIsNumbers(expr.operator, left, right);
                return (double) left >= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if(left instanceof Boolean && right instanceof Boolean) {
            if(expr.operator.type == TokenType.AND) {
                return (boolean) left & (boolean) right;
            }
            if(expr.operator.type == TokenType.OR) {
                return (boolean) left | (boolean) right;
            }
        }
        throw new RuntimeError(expr.operator, "can't evaluate this");
    }

    private void checkIsNumbers(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Non-compatible operands");
    }
    private void execute(Statement stmt) {
        stmt.accept(this);
    }
    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private String stringify(Object value) {
        if(value == null) {
            return "null";
        }
        if(value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        return switch (expr.operator.type) {
            case EMARK -> !isTruthy(right);
            case MINUS -> -(double) right;
            default -> null;
        };
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object bool = evaluate(expr.first);
        if(isTruthy(bool)) {
            return evaluate(expr.left);
        }
        else {
            return evaluate(expr.right);
        }
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return findVariable(expr.name, expr);
    }
    private Object findVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return currentEnv.getAt(distance, name.lexeme);
        } else {
            return globalEnv.get(name);
        }
    }
    private boolean isTruthy(Object object) {
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitExpressionStmt(Statement.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Object visitPrintStmt(Statement.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Statement.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        currentEnv.define(stmt.name.lexeme, value);
        return null;
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            currentEnv.assignAt(distance, expr.name, value);
        } else {
            globalEnv.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitIncDecExpr(Expr.IncDec expr) {
        Object value = evaluate(expr.value);
        Object current = currentEnv.get(expr.name);
        if(current instanceof Double && value instanceof Double) {
            switch(expr.operator.type) {
                case PLUSEQUAL, PLUSPLUS:
                    currentEnv.assign(expr.name, (double) value + (double) current);
                    break;
                case MINUSEQUAL, MINUSMINUS:
                    currentEnv.assign(expr.name, (double) current - (double) value);
                    break;
                case MODEQUAL:
                    currentEnv.assign(expr.name, (double) current % (double) value);
                    break;
                case MULTIPLYEQUAL:
                    currentEnv.assign(expr.name, (double) current * (double) value);
                    break;
                case DIVIDEEQUAL:
                    currentEnv.assign(expr.name, (double) current / (double) value);
                    break;
            }
            return null;
        }
        if(current instanceof String && value instanceof String) {
            if(expr.operator.type == TokenType.PLUSEQUAL) {
                currentEnv.assign(expr.name, (String) value + current);
            }
            return null;
        }
        throw new RuntimeError(expr.operator, "Invalid operation.");
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        List<Object> values = new ArrayList<>();
        for(Expr exprs: expr.list) {
            values.add(evaluate(exprs));
        }
        return values;
    }

    @Override
    public Void visitBlockStmt(Statement.Block stmt) {
        executeBlock(stmt.statements, new Environment(currentEnv));
        return null;
    }
    public void executeBlock(List<Statement> statements,
                      Environment environment) {
        Environment previous = this.currentEnv;
        try {
            this.currentEnv = environment;
            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.currentEnv = previous;
        }
    }
    @Override
    public Void visitIfStmt(Statement.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
    @Override
    public Void visitWhileStmt(Statement.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof JSCallable function)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }
        return function.call(this, arguments);
    }
    @Override
    public Void visitFunctionStmt(Statement.Function stmt) {
        JSFunction function = new JSFunction(stmt, currentEnv, false);
        currentEnv.define(stmt.name.lexeme, function);
        return null;
    }
    @Override
    public Void visitClassStmt(Statement.Class stmt) {
        currentEnv.define(stmt.name.lexeme, null);
        Map<String, JSFunction> methods = new HashMap<>();
        for (Statement.Function method : stmt.methods) {
            JSFunction function = new JSFunction(method, currentEnv, method.name.lexeme.equals("constructor"));
            methods.put(method.name.lexeme, function);
        }
        JSClass klass = new JSClass(stmt.name.lexeme, methods);
        currentEnv.assign(stmt.name, klass);
        return null;
    }
    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof JSInstance) {
            return ((JSInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name,
                "Only instances have properties.");
    }

    @Override
    public Object visitArrayGetExpr(Expr.ArrayGet expr) {
        Object index = evaluate(expr.index);
        Object name = evaluate(expr.name);
        if(index instanceof Double) {
            if((Double) index >= ((List<?>)name).size()) {
                throw new RuntimeError(expr.paren, "Invalid index");
            }
            if(Math.floor((Double) index) != (Double)index) {
                throw new RuntimeError(expr.paren, "Invalid index");
            }
            return ((List<?>)name).get((int) Math.floor((Double)index));
        }
        throw new RuntimeError(expr.paren, "Invalid index");

    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof JSInstance)) {
            throw new RuntimeError(expr.name,
                    "Only instances have fields.");
        }
        Object value = evaluate(expr.value);
        ((JSInstance)object).set(expr.name, value);
        return value;
    }
    @Override
    public Void visitReturnStmt(Statement.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new Return(value);
    }
    @Override
    public Object visitThisExpr(Expr.This expr) {
        return findVariable(expr.keyword, expr);
    }
}