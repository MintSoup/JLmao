package pw.mintsoup.lmao;

import pw.mintsoup.lmao.scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class LmaoInstance {
    LmaoClass klass;
    Map<String, Object> fields = new HashMap<>();

    public LmaoInstance(LmaoClass klass) {
        this.klass = klass;
    }

    public Object get(Token property) {
        if (fields.containsKey(property.lex)) return fields.get(property.lex);
        else {
            if (property.lex.equals(klass.name))
                throw new InstanceError(property, "Cannot refer to constructor from instance");
            LmaoFunction method = klass.findMethod(property.lex);
            if (method != null) return method.bind(this);
        }
        throw new InstanceError(property, "Undefined property: " + property.lex);

    }

    public void set(String lex, Object value) {
        fields.put(lex, value);
    }

    public class InstanceError extends RuntimeException {
        public InstanceError(Token property, String s) {
            super();
            Main.error(property.line, s);
        }
    }
}
