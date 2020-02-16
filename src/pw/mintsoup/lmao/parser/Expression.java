package pw.mintsoup.lmao.parser;
import java.util.List;
import pw.mintsoup.lmao.scanner.Token;
public abstract class Expression{
  interface Visitor<R> {
    R visitBinaryExpression(Binary expression);
    R visitGroupingExpression(Grouping expression);
    R visitLiteralExpression(Literal expression);
    R visitUnaryExpression(Unary expression);
  }
abstract <A> A accept(Visitor<A> visitor);
public static class Binary extends Expression{
final Expression left;
final Expression right;
final Token operand;
public Binary(Expression left, Expression right, Token operand) {
this.left = left;
this.right = right;
this.operand = operand;
}
<A> A accept(Visitor<A> visitor){
 return visitor.visitBinaryExpression(this);
}
}
public static class Grouping extends Expression{
final Expression expression;
public Grouping(Expression expression) {
this.expression = expression;
}
<A> A accept(Visitor<A> visitor){
 return visitor.visitGroupingExpression(this);
}
}
public static class Literal extends Expression{
final Object value;
public Literal(Object value) {
this.value = value;
}
<A> A accept(Visitor<A> visitor){
 return visitor.visitLiteralExpression(this);
}
}
public static class Unary extends Expression{
final Token operand;
final Expression a;
public Unary(Token operand, Expression a) {
this.operand = operand;
this.a = a;
}
<A> A accept(Visitor<A> visitor){
 return visitor.visitUnaryExpression(this);
}
}
}
