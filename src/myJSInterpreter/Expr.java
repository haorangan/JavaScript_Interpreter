package myJSInterpreter;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitIncDecExpr(IncDec expr);
        R visitArrayExpr(Array expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitArrayGetExpr(ArrayGet expr);
        R visitSetExpr(Set expr);
        R visitThisExpr(This expr);
        R visitLogicalExpr(Logical expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitTernaryExpr(Ternary expr);
        R visitVariableExpr(Variable expr);
    }
    public static class Assign extends Expr {
        Assign(Token name, Expr value) {
           this.name = name;
           this.value = value;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignExpr(this);
    }
        final Token name;
        final Expr value;
    }
    public static class IncDec extends Expr {
        IncDec(Token name, Token operator, Expr value) {
           this.name = name;
           this.operator = operator;
           this.value = value;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitIncDecExpr(this);
    }
        final Token name;
        final Token operator;
        final Expr value;
    }
    public static class Array extends Expr {
        Array(List<Expr> list) {
           this.list = list;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitArrayExpr(this);
    }
        final List<Expr> list;
    }
    public static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
           this.left = left;
           this.operator = operator;
           this.right = right;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
        final Expr left;
        final Token operator;
        final Expr right;
    }
    public static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
           this.callee = callee;
           this.paren = paren;
           this.arguments = arguments;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitCallExpr(this);
    }
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
    }
    public static class Get extends Expr {
        Get(Expr object, Token name) {
           this.object = object;
           this.name = name;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitGetExpr(this);
    }
        final Expr object;
        final Token name;
    }
    public static class ArrayGet extends Expr {
        ArrayGet(Token paren, Expr name, Expr index) {
           this.paren = paren;
           this.name = name;
           this.index = index;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitArrayGetExpr(this);
    }
        final Token paren;
        final Expr name;
        final Expr index;
    }
    public static class Set extends Expr {
        Set(Expr object, Token name, Expr value) {
           this.object = object;
           this.name = name;
           this.value = value;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitSetExpr(this);
    }
        final Expr object;
        final Token name;
        final Expr value;
    }
    public static class This extends Expr {
        This(Token keyword) {
           this.keyword = keyword;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitThisExpr(this);
    }
        final Token keyword;
    }
    public static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
           this.left = left;
           this.operator = operator;
           this.right = right;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitLogicalExpr(this);
    }
        final Expr left;
        final Token operator;
        final Expr right;
    }
    public static class Grouping extends Expr {
        Grouping(Expr expression) {
           this.expression = expression;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitGroupingExpr(this);
    }
        final Expr expression;
    }
    public static class Literal extends Expr {
        Literal(Object value) {
           this.value = value;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
        final Object value;
    }
    public static class Unary extends Expr {
        Unary(Token operator, Expr right) {
           this.operator = operator;
           this.right = right;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
        final Token operator;
        final Expr right;
    }
    public static class Ternary extends Expr {
        Ternary(Expr first, Expr left, Expr right) {
           this.first = first;
           this.left = left;
           this.right = right;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitTernaryExpr(this);
    }
        final Expr first;
        final Expr left;
        final Expr right;
    }
    public static class Variable extends Expr {
        Variable(Token name) {
           this.name = name;
        }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariableExpr(this);
    }
        final Token name;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
