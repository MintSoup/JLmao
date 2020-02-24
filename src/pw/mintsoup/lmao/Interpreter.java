package pw.mintsoup.lmao;

import org.jetbrains.annotations.NotNull;
import pw.mintsoup.lmao.parser.Expression;
import pw.mintsoup.lmao.parser.Statement;
import pw.mintsoup.lmao.scanner.TokenType;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    class InterpreterError extends RuntimeException {
    }

    Environment e = new Environment();

    boolean breakFlag = false;

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
        e.define(statement.name, init);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement, new Environment(e));
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
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
        while(isTrue(statement.condition.accept(this))){
            statement.statement.accept(this);
            if(breakFlag) {
                breakFlag = false;
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        if(statement.type.type == TokenType.BREAK) breakFlag = true;
        return null;
    }

    private void executeBlock(Statement.Block statement, Environment environment) {
        e = environment;
        for (Statement s : statement.statements) {
            s.accept(this);
        }
        e = e.parent;
    }


    private InterpreterError error(int line, String where, String message) {
        Main.report(line, where, message);
        return new InterpreterError();
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
                    return false;
                else return !left.equals(right);
            }

            default: {
                throw error(expression.operand.line, "", "lmao parser broken");
            }
        }

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
        return e.get(expression.name);
    }

    @Override
    public Object visitAssignmentExpression(@NotNull Expression.Assignment expression) {
        Object val = expression.value.accept(this);
        e.assign(expression.variable, val);
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

    public boolean isTrue(Object b) {
        if (b == null) return false;
        if (b instanceof Boolean) return (boolean) b;
        return true;
    }


}
