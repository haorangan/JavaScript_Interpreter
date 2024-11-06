package myJSInterpreter;
import java.util.*;

public class JSClass implements JSCallable {
    final String name;
    private final Map<String, JSFunction> methods;
    JSClass(String name, Map<String, JSFunction> methods) {
        this.name = name;
        this.methods = methods;
    }
    JSFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }
    @Override
    public String toString() {
        return name;
    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        JSInstance instance = new JSInstance(this);
        JSFunction initializer = findMethod("constructor");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
    @Override
    public int arity() {
        JSFunction initializer = findMethod("constructor");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
