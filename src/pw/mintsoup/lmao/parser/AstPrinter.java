package pw.mintsoup.lmao.parser;

public class AstPrinter implements Expression.Visitor {

    public void print(Expression f) {
        f.accept(this);
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        System.out.print("[");
        expression.left.accept(this);
        System.out.print(" ");
        System.out.print(expression.operand.lex);
        System.out.print(" ");
        expression.right.accept(this);
        System.out.print("]");
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping group) {
        System.out.print("group( ");
        group.expression.accept(this);
        System.out.print(" )");
        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        System.out.print(expression.value);
        return null;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        System.out.print("(" + expression.operand.lex);
        expression.a.accept(this);
        System.out.print(")");

        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return null;
    }

    @Override
    public Object visitAssignmentExpression(Expression.Assignment expression) {
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        return null;
    }


}
