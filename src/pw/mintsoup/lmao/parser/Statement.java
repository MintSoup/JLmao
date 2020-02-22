package pw.mintsoup.lmao.parser;
import java.util.List;
import pw.mintsoup.lmao.scanner.Token;
public abstract class Statement{
 public interface Visitor<R> {
    R visitEStatementStatement(EStatement statement);
    R visitPrintStatement(Print statement);
    R visitVarStatement(Var statement);
  }
public abstract <A> A accept(Visitor<A> visitor);
public static class EStatement extends Statement{
public final Expression e;
public EStatement(Expression e) {
this.e = e;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitEStatementStatement(this);
}
}
public static class Print extends Statement{
public final Expression e;
public Print(Expression e) {
this.e = e;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitPrintStatement(this);
}
}
public static class Var extends Statement{
public final Token name;
public final Expression init;
public Var(Token name, Expression init) {
this.name = name;
this.init = init;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitVarStatement(this);
}
}
}
