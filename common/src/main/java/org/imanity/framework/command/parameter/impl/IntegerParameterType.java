package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class IntegerParameterType implements ParameterHolder<Integer> {

	@Override
	public Class[] type() {
		return new Class[] {Integer.class, int.class};
	}

	public Integer transform(CommandEvent commandEvent, String source) {
		try {
			return (Integer.parseInt(source));
		} catch (NumberFormatException exception) {
			commandEvent.sendInternalError(source + " is not a valid number.");
			return null;
		}
	}

}