package pw.mintsoup.lmao;

import org.jetbrains.annotations.NotNull;
import pw.mintsoup.lmao.scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Map<String, Object> vars = new HashMap<>();

    public Environment parent;

    class EnvironmentError extends RuntimeException {
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment() {
        this(null);
    }

    private EnvironmentError error(int line, String where, String message) {
        Main.report(line, where, message);
        return new EnvironmentError();
    }

    public void define(Token name, Object value) {
        vars.put(name.lex, value);
    }

    public Object get(@NotNull Token name) {
        if (vars.containsKey(name.lex)) return vars.get(name.lex);
        else if (parent != null)
            return parent.get(name);
        else
            throw error(name.line, "at '" + name.lex + "'", "Undefined variable: " + name.lex);

    }

    public void assign(Token name, Object val) {
        if (vars.containsKey(name.lex)) vars.put(name.lex, val);
        else if (parent != null && parent.vars.containsKey(name.lex)) parent.vars.put(name.lex, val);
        else throw error(name.line, "at '" + name.lex + "'", "Undefined variable: " + name.lex);
    }
}
