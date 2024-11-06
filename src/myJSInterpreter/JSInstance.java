package myJSInterpreter;
import java.util.*;

public class JSInstance {
    private final Map<String, Object> fields = new HashMap<>();
    private JSClass klass;
    JSInstance(JSClass klass) {
        this.klass = klass;
    }
    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        JSFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method;
        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

}
