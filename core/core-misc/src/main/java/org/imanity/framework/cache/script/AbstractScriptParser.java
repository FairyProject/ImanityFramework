package org.imanity.framework.cache.script;

import org.intellij.lang.annotations.Language;

import java.lang.reflect.Method;

public abstract class AbstractScriptParser {

    protected static final String TARGET = "target";

    protected static final String ARGS = "args";

    protected static final String RET_VAL = "retVal";

    protected static final String HASH = "hash";

    protected static final String EMPTY = "empty";

    public String getDefinedCacheKey(String keyEL, Object target, Object[] arguments, Object retVal, boolean hasRetVal)
            throws Exception {
        return this.getElValue(keyEL, target, arguments, retVal, hasRetVal, String.class);
    }

    public <T> T getElValue(String keyEL, Object target, Object[] arguments, Class<T> valueType) throws Exception {
        return this.getElValue(keyEL, target, arguments, null, false, valueType);
    }

    public abstract <T> T getElValue(@Language("JavaScript") String exp, Object target, Object[] arguments, Object retVal, boolean hasRetVal,
                                     Class<T> valueType) throws Exception;

    public abstract void addFunction(String name, Method method);

}
