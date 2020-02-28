package pw.mintsoup.lmao;

import java.util.List;

public interface LmaoCallable {
    Object call(Interpreter interpreter, List<Object> args);

    int argSize();
}
