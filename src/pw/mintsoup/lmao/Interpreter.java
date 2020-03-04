package pw.mintsoup.lmao;

import org.jetbrains.annotations.NotNull;
import pw.mintsoup.lmao.parser.Expression;
import pw.mintsoup.lmao.parser.Statement;
import pw.mintsoup.lmao.scanner.Token;
import pw.mintsoup.lmao.scanner.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    class InterpreterError extends RuntimeException {
    }

    public final Environment globals = new Environment();
    Environment e = globals;

    private final Map<Expression, Integer> locals = new HashMap<>();

    final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public void resolve(Expression expression, int i) {
        locals.put(expression, i);
    }


    @Override
    public Void visitEStatementStatement(@NotNull Statement.EStatement statement) {
        statement.e.accept(this);
        return null;
    }

    @Override
    public Void visitPrintStatement(@NotNull Statement.Print statement) {
        System.out.println(statement.e.accept(this));
        return null;
    }

    @Override
    public Void visitVarStatement(@NotNull Statement.Var statement) {
        Object init = null;
        if (statement.init != null) {
            init = statement.init.accept(this);
        }
        e.define(statement.name.lex, init);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement, new Environment(e));
        return null;
    }

    public Interpreter() {
        globals.define("clock", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double) System.currentTimeMillis();
            }

            @Override
            public int argSize() {
                return 0;
            }
        });
        globals.define("readln", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                try {
                    return in.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return null;

            }

            @Override
            public int argSize() {
                return 0;
            }
        });
        globals.define("str", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                if (args.get(0) == null) return "null";
                return args.get(0).toString();
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
        globals.define("istr", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                if (args.get(0) == null) return "null";
                if (isInt((double) args.get(0))) {
                    return (int) ((double) args.get(0)) + "";
                }
                return args.get(0).toString();
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
        globals.define("sqrt", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                if (args.get(0) == null) return "null";
                if (args.get(0) instanceof Double) {
                    return Math.sqrt((double) args.get(0));
                } else return null;
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
        globals.define("map", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return new HashMap<>();
            }

            @Override
            public int argSize() {
                return 0;
            }
        });
        globals.define("size", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                if (args.get(0) instanceof String) return (double) ((String) args.get(0)).length();
                else if (args.get(0) instanceof Map) return (double) args.size();
                else return null;
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
        globals.define("printbyte", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object byt = args.get(0);
                if (byt instanceof Double) {
                    if (isInt((double) byt)) {
                        int b = (int) Math.round((double) byt);
                        if (b >= 0 && b <= 255) {
                            System.out.print((char) b);
                        }
                    }
                }
                return null;
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
        globals.define("int", new LmaoCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object byt = args.get(0);
                if (byt instanceof String && ((String) byt).length() == 1) {
                    return (double) ((int) ((String) byt).charAt(0));
                }
                return byt;
            }

            @Override
            public int argSize() {
                return 1;
            }
        });
    }

    @Override
    public Void visitIfStatement(@NotNull Statement.If statement) {
        if (isTrue(statement.condition.accept(this))) {
            statement.statement.accept(this);
        } else {
            if (statement.elseStatement != null) {
                statement.elseStatement.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTrue(statement.condition.accept(this))) {
            try {
                statement.statement.accept(this);
            } catch (Break b) {
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        throw new Break();
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        LmaoFunction f = new LmaoFunction(statement, e);
        e.define(f.declaration.name.lex, f);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if(statement.value == null) throw new Return(null);
        throw new Return(statement.value.accept(this));
    }

    public void executeBlock(Statement.Block statement, Environment environment) {
        Environment previous = e;
        try {
            e = environment;
            for (Statement s : statement.statements) {
                s.accept(this);
            }
        } finally {
            e = previous;

        }

    }


    private InterpreterError error(int line, String where, String message) {
        Main.report(line, where, message);
        return new InterpreterError();
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        e.define(statement.name.lex, null);
        Map<String, LmaoFunction> methods = new HashMap<>();
        for(Statement.Function f : statement.functions){
            methods.put(f.name.lex, new LmaoFunction(f, e));
        }
        LmaoClass klass = new LmaoClass(statement, methods);
        e.assign(statement.name, klass);
        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = expression.left.accept(this);
        Object right = expression.right.accept(this);
        TokenType operand = expression.operand.type;

        switch (operand) {
            case PLUS: {
                if (left instanceof Double) {
                    if (right instanceof Double) {
                        return (double) left + (double) right;
                    } else {
                        throw error(expression.operand.line, "at '+'", "using + operand on unsupported operands");
                    }
                } else if (left instanceof String) {
                    if (right instanceof String) {
                        return (String) left + (String) right;

                    } else {
                        throw error(expression.operand.line, "at '+'", "using + operand on unsupported operands");
                    }

                } else {
                    throw error(expression.operand.line, "at '+'", "using + operand on unsupported operands");
                }
            }
            case MINUS: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                } else throw error(expression.operand.line, "at '-'", "'-' Operator not supported for those types");
            }
            case STAR: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left * (double) right;
                } else throw error(expression.operand.line, "at '*'", "'*' Operator not supported for those types");
            }
            case SLASH: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left / (double) right;
                } else throw error(expression.operand.line, "at '/'", "'/' Operator not supported for those types");
            }
            case GREATER: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                } else throw error(expression.operand.line, "at '>'", "'>' Operator not supported for those types");

            }
            case GREATER_EQUAL: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                } else
                    throw error(expression.operand.line, "at '>='", "'>=' Operator not supported for those types");

            }
            case LESS: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                } else throw error(expression.operand.line, "at '<'", "'<' Operator not supported for those types");

            }
            case LESS_EQUAL: {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                } else
                    throw error(expression.operand.line, "at '<='", "'<=' Operator not supported for those types");

            }
            case DBL_EQUAL: {
                if (left == null)
                    return right == null;
                if (right == null) return false;
                else return left.equals(right);

            }
            case NOT_EQUAL: {
                if (left == null)
                    return right != null;
                else if (right == null)
                    return true;
                else return !left.equals(right);
            }
            case MODULO: {
                if (left instanceof Double && right instanceof Double) {
                    double l = (double) left;
                    double r = (double) right;
                    if (isInt(l) && isInt(r)) {
                        return (double) (Math.round(l) % Math.round(r));
                    } else {
                        throw error(expression.operand.line, "at %", "Cannot modulo two doubles.");
                    }

                } else {
                    throw error(expression.operand.line, "at %", "Using modulo on unsupported operands.");
                }
            }

            default: {
                throw error(expression.operand.line, "", "lmao parser broken");
            }
        }

    }

    private boolean isInt(double d) {
        double difference = Math.abs(Math.round(d) - d);
        return difference < 0.000001;
    }

    @Override
    public Object visitGroupingExpression(@NotNull Expression.Grouping expression) {
        return expression.expression.accept(this);
    }

    @Override
    public Object visitLiteralExpression(@NotNull Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(@NotNull Expression.Unary expression) {
        Object value = expression.a.accept(this);

        if (expression.operand.type == TokenType.NOT) {
            return !isTrue(value);
        } else if (expression.operand.type == TokenType.MINUS) {
            if (value instanceof Double) {
                return -(double) value;
            } else {
                throw error(expression.operand.line, "", "The '-' operator is not defined for the data type you're using.");
            }


        }
        throw error(expression.operand.line, "", "lmao what");
    }

    @Override
    public Object visitVariableExpression(@NotNull Expression.Variable expression) {
        return lookupVariable(expression, expression.name);
    }

    private Object lookupVariable(Expression ex, Token name) {
        Integer depth = locals.get(ex);
        if (depth != null) {
            return this.e.getAt(depth, name);
        }
        return globals.get(name);

    }

    @Override
    public Object visitAssignmentExpression(@NotNull Expression.Assignment expression) {
        Object val = expression.value.accept(this);

        Integer distance = locals.get(expression);
        if (expression.index == null) {
            if (distance != null) {
                e.assignAt(distance, expression.variable, val);
            } else {
                globals.assign(expression.variable, val);
            }
        } else {
            Object map = lookupVariable(expression, expression.variable);
            if (map instanceof Map) {
                Map var = (Map) map;
                var.put(expression.index.accept(this), val);
            } else
                throw error(expression.variable.line, "at '" + expression.variable.lex + "'", "Only maps are mutable");
        }


        return val;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = expression.left.accept(this);
        Object right = expression.right.accept(this);

        switch (expression.operand.type) {
            case AND: {
                return isTrue(left) && isTrue(right);
            }
            case OR: {
                return isTrue(left) || isTrue(right);
            }
            case XOR: {
                return isTrue(left) != isTrue(right);
            }
            default: {
                throw error(expression.operand.line, "ur mom", "for some cringe ass reason i got a " + expression.operand.type + " for a logical expression");
            }
        }
    }

    @Override
    public Object visitCallExpression(@NotNull Expression.Call expression) {
        Object c = expression.callee.accept(this);
        if (!(c instanceof LmaoCallable))
            throw error(expression.p.line, "at function call", "Tried calling a non callable");
        List<Object> args = new ArrayList<>();
        for (Expression e : expression.arguments) {
            args.add(e.accept(this));
        }
        LmaoCallable func = (LmaoCallable) c;
        if (args.size() != func.argSize()) {
            throw error(expression.p.line, "at function call", "Expected " + func.argSize() + " arguments, got " + args.size() + ".");
        }
        return func.call(this, args);
    }

    @Override
    public Object visitMapExpression(Expression.Map expression) {
        Object index = expression.index.accept(this);
        Object pointer = expression.var.accept(this);
        if (pointer instanceof Map) {
            return ((Map) pointer).get(index);
        } else if (pointer instanceof String) {
            if (!(index instanceof Double && isInt((double) index) && Math.round((double) index) >= 0)) {
                throw error(expression.nameToken.line, "at '" + expression.nameToken.lex + "' ", "Strings only can only have positive integer indexes");
            }
            try {
                return ((String) pointer).charAt((int) Math.round((double) index)) + "";
            } catch (StringIndexOutOfBoundsException e) {
                return null;
            }
        } else {
            throw error(expression.nameToken.line, "at '" + expression.nameToken.lex + "' ", "Not a string or map");
        }
    }

    @Override
    public Object visitSetExpression(Expression.Set expression) {
        Object obj = expression.object.accept(this);
        if (!(obj instanceof LmaoInstance))
            throw error(expression.name.line, "at '" + expression.name.lex + "'", "Only instances have fields.");
        else{
            Object value = expression.value.accept(this);
            LmaoInstance instance = (LmaoInstance) obj;
            instance.set(expression.name.lex, value);
            return value;
        }
    }

    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookupVariable(expression, expression.keyword);
    }

    @Override
    public Object visitGetExpression(Expression.Get expression) {
        Object obj = expression.object.accept(this);
        if (obj instanceof LmaoInstance) {
            return ((LmaoInstance) obj).get(expression.name);
        } else throw error(expression.name.line, "at '" + expression.name.lex + "'", "Only instances have properties");
    }


    public boolean isTrue(Object b) {
        if (b == null) return false;
        if (b instanceof Boolean) return (boolean) b;
        return true;
    }


    public class Return extends RuntimeException {
        Object value;

        public Return(Object value) {
            this.value = value;
        }
    }

    public class Break extends RuntimeException {
    }
}
