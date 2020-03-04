package pw.mintsoup.lmao;

import pw.mintsoup.lmao.parser.Statement;

import java.util.List;
import java.util.Map;

public class LmaoClass implements LmaoCallable {
    public final String name;
    protected Map<String, LmaoFunction> methods;

    public LmaoClass(Statement.Class statement, Map<String, LmaoFunction> methods) {
        this.name = statement.name.lex;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        LmaoInstance i = new LmaoInstance(this);
        LmaoFunction initializer = findMethod(name);
        if (initializer != null)
            initializer.bind(i).call(interpreter, args);

        return i;
    }

    @Override
    public int argSize() {
        LmaoFunction init = findMethod(name);
        if(init != null) return init.argSize();
        else return 0;
    }

    public LmaoFunction findMethod(String method) {
        if (methods.containsKey(method)) return methods.get(method);
        else return null;
    }
}
