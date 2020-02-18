package pw.mintsoup.lmao;

import org.jetbrains.annotations.NotNull;
import pw.mintsoup.lmao.parser.Expression;
import pw.mintsoup.lmao.scanner.TokenType;

public class Interpreter implements Expression.Visitor {
    class InterpreterError extends RuntimeException {
    }

    private InterpreterError error(int line, String where, String message) {
        Main.report(line, where, message);
        return new InterpreterError();
    }

    @Override
    public Object visitBinaryExpression(@NotNull Expression.Binary expression) {
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
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return expression.expression.accept(this);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object value = expression.a.accept(this);

        if (expression.operand.type == TokenType.NOT) {
            if (value instanceof Boolean) {
                return !(boolean) value;
            } else if (value == null) {
                return false;
            } else {
                return true;
            }
        } else if (expression.operand.type == TokenType.MINUS) {
            if (value instanceof Double) {
                return -(double) value;
            } else {
                throw error(expression.operand.line, "", "The '-' operator is not defined for the data type you're using.");
            }


        }
        throw error(expression.operand.line, "", "lmao what");
    }
}
