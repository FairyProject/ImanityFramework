package org.imanity.framework.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.imanity.framework.command.parameter.ParameterMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class CommandMeta {

    private final String[] names;
    private final String permission;
    private final List<ParameterMeta> parameters;
    private final Object instance;
    private final Method method;

    public String getName() {
        return names[0];
    }

    public boolean canAccess(Object user) {
        if (this.permission == null || this.permission.length() == 0) {
            return true;
        }

        return CommandService.INSTANCE.getProvider().hasPermission(user, this.permission);
    }

    public String getUsage() {
        return this.getUsage(this.getName());
    }

    public String getUsage(String aliasUsed) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ParameterMeta parameterMeta : getParameters()) {
            boolean needed = parameterMeta.getDefaultValue().isEmpty();
            stringBuilder
                    .append(needed ? "<" : "[").append(parameterMeta.getName())
                    .append(needed ? ">" : "]").append(" ");
        }

        return ("/" + aliasUsed.toLowerCase() + " " + stringBuilder.toString().trim().toLowerCase());
    }

    public void execute(InternalCommandEvent event, String[] arguments) {
        List<Object> transformedParameters = new ArrayList<>();

        transformedParameters.add(event);

        for (int i = 0; i < this.getParameters().size(); i++) {
            ParameterMeta parameter = getParameters().get(i);
            String passedParameter = (i < arguments.length ? arguments[i] : parameter.getDefaultValue()).trim();
            if (i >= arguments.length &&
                    (parameter.getDefaultValue() == null || parameter.getDefaultValue().isEmpty())) {
                CommandService.INSTANCE.getProvider().sendUsage(event, this.getUsage());
                return;
            }
            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(arguments, i);
            }
            Object result = CommandService.INSTANCE.transformParameter(event, passedParameter, parameter.getParameterClass());
            if (result == null) {
                return;
            }
            transformedParameters.add(result);
            if (parameter.isWildcard()) {
                break;
            }
        }

        try {
            method.invoke(this.instance, transformedParameters.toArray());
        } catch (Exception e) {
            CommandService.INSTANCE.getProvider().sendError(event, e);
            e.printStackTrace();
        }
    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}
