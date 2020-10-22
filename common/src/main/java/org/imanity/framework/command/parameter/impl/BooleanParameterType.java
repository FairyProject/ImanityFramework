package org.imanity.framework.command.parameter.impl;

import com.google.common.collect.ImmutableMap;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

import java.util.Map;

@Component
public class BooleanParameterType implements ParameterHolder<Boolean> {

    private static final Map<String, Boolean> MAP;

    static {
        MAP = ImmutableMap.<String, Boolean>builder()
                .put("true", true)
                .put("on", true)
                .put("yes", true)
                .put("false", false)
                .put("off", false)
                .put("no", false)
        .build();
    }

    @Override
    public Class[] type() {
        return new Class[] {Boolean.class, boolean.class};
    }

    public Boolean transform(CommandEvent event, String source) {
        if (!MAP.containsKey(source.toLowerCase())) {
            event.sendInternalError(source + " is not a valid boolean.");
            return (null);
        }

        return MAP.get(source.toLowerCase());
    }

}