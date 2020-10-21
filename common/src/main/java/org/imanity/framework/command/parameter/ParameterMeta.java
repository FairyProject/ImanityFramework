package org.imanity.framework.command.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ParameterMeta {

    private final String name;
    private final boolean wildcard;
    private final String defaultValue;
    private final String[] tabCompleteFlags;
    private final Class<?> parameterClass;

}
