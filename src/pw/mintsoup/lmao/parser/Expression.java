package pw.mintsoup.lmao.parser;
import java.util.List;
import pw.mintsoup.lmao.scanner.Token;
public abstract class Expression{
 public interface Visitor<R> {
    R visitBinaryExpression(Binary expression);
    R visitGroupingExpression(Grouping expression);
    R visitLiteralExpression(Literal expression);
    R visitUnaryExpression(Unary expression);
  }
public abstract <A> A accept(Visitor<A> visitor);
public static class Binary extends Expression{
public final Expression left;
public final Expression right;
public final Token operand;
public Binary(Expression left, Expression right, Token operand) {
this.left = left;
this.right = right;
this.operand = operand;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitBinaryExpression(this);
}
}
public static class Grouping extends Expression{
public final Expression expression;
public Grouping(Expression expression) {
this.expression = expression;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitGroupingExpression(this);
}
}
public static class Literal extends Expression{
public final Object value;
public Literal(Object value) {
this.value = value;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitLiteralExpression(this);
}
}
public static class Unary extends Expression{
public final Token operand;
public final Expression a;
public Unary(Token operand, Expression a) {
this.operand = operand;
this.a = a;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitUnaryExpression(this);
}
}
}
