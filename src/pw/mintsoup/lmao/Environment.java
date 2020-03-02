package pw.mintsoup.lmao;

import org.jetbrains.annotations.NotNull;
import pw.mintsoup.lmao.scanner.Token;

import javax.swing.text.html.parser.AttributeList;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Map<String, Object> vars = new HashMap<>();

    public Environment parent;

    public Object getAt(Integer depth, Token name) {
        return ancestor(depth).vars.get(name.lex);
    }

    public void assignAt(Integer depth, Token name, Object value) {
        ancestor(depth).vars.put(name.lex, value);
    }

    private Environment ancestor(Integer depth) {
        Environment e = this;
        for (int i = 0; i < depth; i++) {
            e = e.parent;
        }
        return e;
    }

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

    public void define(String name, Object value) {
        vars.put(name, value);
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
        else if (parent != null) parent.assign(name, val);
        else throw error(name.line, "at '" + name.lex + "'", "Undefined variable: " + name.lex);
    }
}
