package pw.mintsoup.lmao.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class AstGenerator {
    static String output = "src/pw/mintsoup/lmao/parser";

    public static void main(String[] args) throws IOException {
        defineAST("Expression", (List<String>) Arrays.asList("Binary: Expression left, Expression right, Token operand",
                "Grouping: Expression expression",
                "Literal: Object value",
                "Unary: Token operand, Expression a"));
    }

    private static void defineAST(String baseName, List<String> types) throws FileNotFoundException {
        FileOutputStream out = new FileOutputStream(output + "/" + baseName + ".java");
        PrintWriter wrt = new PrintWriter(out);

        wrt.println("package pw.mintsoup.lmao.parser;");
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
        wrt.println("public static class " + className + " extends Expression" + "{");
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
