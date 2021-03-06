package pw.mintsoup.lmao.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class AstGenerator {

    public static void main(String[] args) throws IOException {
        defineAST("pw.mintsoup.lmao.parser", "Expression", Arrays.asList("Binary: Expression left, Expression right, Token operand",
                "Grouping: Expression expression",
                "Literal: Object value",
                "Unary: Token operand, Expression a",
                "Variable: Token name",
                "Assignment: Token variable, Expression value, Expression index",
                "Logical: Expression left, Expression right, Token operand",
                "Call: Expression callee, Token p, List<Expression> arguments",
                "Map: Expression var, Expression index, Token nameToken",
                "Get: Expression object, Token name",
                "Set: Expression object, Token name, Expression value",
                "This: Token keyword"));
        defineAST("pw.mintsoup.lmao.parser", "Statement", Arrays.asList("EStatement: Expression e",
                "Print: Expression e",
                "Var: Token name, Expression init",
                "Block: List<Statement> statements",
                "If: Expression condition, Statement statement, Statement elseStatement",
                "While: Expression condition, Statement statement",
                "Break: Token type",
                "Function: Token name, List<Token> arguments, List<Statement> statements",
                "Return: Expression value, Token keyword",
                "Class: Token name, List<Statement.Function> functions"
        ));
    }

    private static void defineAST(String pkg, String baseName, List<String> types) throws FileNotFoundException {
        PrintWriter wrt = new PrintWriter("src/" + pkg.replace('.', '\\') + "\\" + baseName + ".java");

        wrt.println("package " + pkg + ";");
        wrt.println("import java.util.List;");
        wrt.println("import pw.mintsoup.lmao.scanner.Token;");

        wrt.println("public abstract class " + baseName + "{");


        defineVisitors(wrt, baseName, types);
        wrt.println("public abstract <A> A accept(Visitor<A> visitor);");


        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(wrt, baseName, className, fields);
        }

        wrt.println("}");


        wrt.close();

    }

    private static void defineVisitors(PrintWriter wrt, String baseName, List<String> types) {
        wrt.println(" public interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            wrt.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }
        wrt.println("  }");
    }

    private static void defineType(PrintWriter wrt, String baseName, String className, String fields) {
        wrt.println("public static class " + className + " extends " + baseName + "{");
        for (String expr : fields.split(",")) {
            expr = expr.trim();
            wrt.println("public final " + expr + ";");
        }

        wrt.println("public " + className + "(" + fields + ") {");

        for (String expr : fields.split(",")) {
            expr = expr.trim();
            String name = expr.split(" ")[1];
            wrt.println("this." + name + " = " + name + ";");
        }


        wrt.println("}");

        wrt.println("public <A> A accept(Visitor<A> visitor){");
        wrt.println(" return visitor.visit" + className + baseName + "(this);");
        wrt.println("}");
        wrt.println("}");


    }


}
