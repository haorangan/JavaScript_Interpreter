package myJSInterpreter;
import java.util.*;

public interface JSCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
