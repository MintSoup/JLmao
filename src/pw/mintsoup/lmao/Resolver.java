package pw.mintsoup.lmao;

import com.sun.corba.se.spi.ior.IdentifiableFactory;
import pw.mintsoup.lmao.parser.Expression;
import pw.mintsoup.lmao.parser.Statement;
import pw.mintsoup.lmao.scanner.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Statement.Visitor<Void>, Expression.Visitor<Object> {
    Interpreter interpreter;
    final Stack<Map<String, Boolean>> scopes;
    private FunctionType currentFunction = FunctionType.NONE;
    private boolean insideLoop = false;

    private enum FunctionType {
        NONE, FUNCTION
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        scopes = new Stack<>();
    }


    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.a);
        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() && scopes.peek().get(expression.name) == Boolean.FALSE) {
            Main.error(expression.name.line, "Cannot access local variable in its own initializer");
        }
        resolveLocal(expression, expression.name);
        return null;
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lex)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }

    }

    @Override
    public Object visitAssignmentExpression(Expression.Assignment expression) {
        resolveLocal(expression, expression.variable);
        if (expression.index != null)
            resolve(expression.index);
        resolve(expression.value);
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Object visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);
        for (Expression e : expression.arguments) {
            resolve(e);
        }
        return null;
    }

    @Override
    public Object visitMapExpression(Expression.Map expression) {
        return null;
    }

    @Override
    public Void visitEStatementStatement(Statement.EStatement statement) {
        resolve(statement.e);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.e);
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        declare(statement.name);
        if (statement.init != null) {
            resolve(statement.init);
        }
        define(statement.name);
        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lex, true);
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        if (!scopes.peek().containsKey(name.lex)) {
            scopes.peek().put(name.lex, false);
        } else {
            Main.error(name.line, "Variable already declared in this scope.");
        }
    }

    private void resolve(Expression init) {
        init.accept(this);
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements);
        endScope();

        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    public void resolve(List<Statement> statements) {
        for (Statement s : statements) {
            resolve(s);
        }
    }

    private void resolve(Statement s) {
        s.accept(this);
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.statement);
        if (statement.elseStatement != null)
            resolve(statement.elseStatement);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        boolean wasInsideLoop = insideLoop;
        insideLoop = true;
        resolve(statement.condition);
        resolve(statement.statement);
        insideLoop = wasInsideLoop;
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        if (!insideLoop) Main.error(statement.type.line, "Break outside loop.");
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name);
        define(statement.name);
        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Statement.Function statement, FunctionType function) {
        FunctionType parentFunction = currentFunction;
        currentFunction = function;
        beginScope();
        for (Token param : statement.arguments) {
            declare(param);
            define(param);
        }
        resolve(statement.statements);
        endScope();
        currentFunction = parentFunction;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Main.error(statement.keyword.line, "Return outside function");
        }
        if (statement.value != null) resolve(statement.value);
        return null;
    }
}
