package org.imanity.framework.command.parameter.impl;

import com.google.common.collect.ImmutableMap;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

import java.util.Map;

@Component
public class BooleanParameterType implements ParameterHolder<Boolean> {

    private static final Map<String, Boolean> MAP;

    @Autowired
    private CommandService commandService;

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

    public Boolean transform(InternalCommandEvent event, String source) {
        if (!MAP.containsKey(source.toLowerCase())) {
            this.commandService.getProvider().sendInternalError(event, source + " is not a valid boolean.");
            return (null);
        }

        return MAP.get(source.toLowerCase());
    }

}