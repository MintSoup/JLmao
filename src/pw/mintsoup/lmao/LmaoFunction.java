package pw.mintsoup.lmao;

import pw.mintsoup.lmao.parser.Statement;

import java.util.List;

public class LmaoFunction implements LmaoCallable {
    Statement.Function declaration;
    Environment parent;

    public LmaoFunction(Statement.Function declaration, Environment parent) {
        this.declaration = declaration;
        this.parent = parent;
    }


    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment e = new Environment(parent);
        for (int i = 0; i < declaration.arguments.size(); i++) {
            e.define(declaration.arguments.get(i).lex, args.get(i));
        }
        try {
            interpreter.executeBlock(new Statement.Block(declaration.statements), e);
        } catch (Interpreter.Return r) {
            return r.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lex + ">";
    }

    @Override
    public int argSize() {
        return declaration.arguments.size();
    }
}
