package org.imanity.framework.bukkit.command.param;

import lombok.Getter;
import org.imanity.framework.bukkit.command.param.Parameter;

public final class ParameterData {

	@Getter
    private String name;
	@Getter
    private boolean wildcard;
	@Getter
    private String defaultValue;
	@Getter
    private String[] tabCompleteFlags;
	@Getter
    private Class<?> parameterClass;

	public ParameterData(Parameter parameterAnnotation, Class<?> parameterClass) {
		this.name = parameterAnnotation.name();
		this.wildcard = parameterAnnotation.wildcard();
		this.defaultValue = parameterAnnotation.defaultValue();
		this.tabCompleteFlags = parameterAnnotation.tabCompleteFlags();
		this.parameterClass = parameterClass;
	}

}