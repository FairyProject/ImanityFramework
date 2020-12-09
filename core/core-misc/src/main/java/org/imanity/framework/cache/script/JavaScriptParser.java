package org.imanity.framework.cache.script;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.cache.CacheUtil;
import org.intellij.lang.annotations.Language;

import javax.script.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class JavaScriptParser extends AbstractScriptParser {

    private static final Logger LOGGER = LogManager.getLogger(JavaScriptParser.class);

    private final ScriptEngineManager engineManager = new ScriptEngineManager();

    private final ConcurrentHashMap<String, CompiledScript> expressenCache = new ConcurrentHashMap<>();
    private final StringBuilder functions = new StringBuilder();

    private final ScriptEngine engine;

    public JavaScriptParser() {
        this.engine = this.engineManager.getEngineByName("javascript");

        try {
            this.addFunction(HASH, CacheUtil.class.getDeclaredMethod("getUniqueHashString", Object.class));
            this.addFunction(EMPTY, CacheUtil.class.getDeclaredMethod("isEmpty", Object.class));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void addFunction(String name, Method method) {
        try {
            String clsName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            functions.append("function ")
                    .append(name)
                    .append("(obj){return ")
                    .append(clsName)
                    .append(".")
                    .append(methodName)
                    .append("(obj);}");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T getElValue(@Language("JavaScript") String exp, Object target, Object[] arguments, Object retVal, boolean hasRetVal, Class<T> valueType) throws Exception {
        Bindings bindings = new SimpleBindings();
        bindings.put(TARGET, target);
        bindings.put(ARGS, arguments);
        if (hasRetVal) {
            bindings.put(RET_VAL, retVal);
        }
        CompiledScript script = this.expressenCache.get(exp);
        if (null != script) {
            return (T) script.eval(bindings);
        }
        if (engine instanceof Compilable) {
            Compilable compEngine = (Compilable) engine;
            script = compEngine.compile(functions + exp);
            expressenCache.put(exp, script);
            return (T) script.eval(bindings);
        } else {
            return (T) engine.eval(functions + exp, bindings);
        }
    }
}
