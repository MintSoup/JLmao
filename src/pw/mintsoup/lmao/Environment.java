package pw.mintsoup.lmao;

import org.omg.CORBA.OBJECT_NOT_EXIST;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Map<String, Object> vars = new HashMap<>();


    public void define(String name, Object value){
        vars.put(name, value);
    }

    public Object get(String name){
        if(vars.containsKey(name)) return vars.get(name);
        else throw new RuntimeException("Undefined variable: " + name);

    }

    public void assign(String name, Object val) {
        if(vars.containsKey(name)) vars.put(name, val);
        else throw new RuntimeException("Undefined variable: " + name);
    }
}
