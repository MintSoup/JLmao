package pw.mintsoup.lmao.parser;
import java.util.List;
import pw.mintsoup.lmao.scanner.Token;
public abstract class Statement{
 public interface Visitor<R> {
    R visitEStatementStatement(EStatement statement);
    R visitPrintStatement(Print statement);
    R visitVarStatement(Var statement);
    R visitBlockStatement(Block statement);
    R visitIfStatement(If statement);
    R visitWhileStatement(While statement);
    R visitBreakStatement(Break statement);
    R visitFunctionStatement(Function statement);
    R visitReturnStatement(Return statement);
    R visitClassStatement(Class statement);
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
public static class Block extends Statement{
public final List<Statement> statements;
public Block(List<Statement> statements) {
this.statements = statements;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitBlockStatement(this);
}
}
public static class If extends Statement{
public final Expression condition;
public final Statement statement;
public final Statement elseStatement;
public If(Expression condition, Statement statement, Statement elseStatement) {
this.condition = condition;
this.statement = statement;
this.elseStatement = elseStatement;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitIfStatement(this);
}
}
public static class While extends Statement{
public final Expression condition;
public final Statement statement;
public While(Expression condition, Statement statement) {
this.condition = condition;
this.statement = statement;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitWhileStatement(this);
}
}
public static class Break extends Statement{
public final Token type;
public Break(Token type) {
this.type = type;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitBreakStatement(this);
}
}
public static class Function extends Statement{
public final Token name;
public final List<Token> arguments;
public final List<Statement> statements;
public Function(Token name, List<Token> arguments, List<Statement> statements) {
this.name = name;
this.arguments = arguments;
this.statements = statements;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitFunctionStatement(this);
}
}
public static class Return extends Statement{
public final Expression value;
public final Token keyword;
public Return(Expression value, Token keyword) {
this.value = value;
this.keyword = keyword;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitReturnStatement(this);
}
}
public static class Class extends Statement{
public final Token name;
public final List<Statement.Function> functions;
public Class(Token name, List<Statement.Function> functions) {
this.name = name;
this.functions = functions;
}
public <A> A accept(Visitor<A> visitor){
 return visitor.visitClassStatement(this);
}
}
}
