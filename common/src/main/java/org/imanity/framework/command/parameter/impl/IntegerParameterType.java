package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class IntegerParameterType implements ParameterHolder<Integer> {

	@Autowired
	private CommandService commandService;

	@Override
	public Class[] type() {
		return new Class[] {Integer.class, int.class};
	}

	public Integer transform(InternalCommandEvent commandEvent, String source) {
		try {
			return (Integer.parseInt(source));
		} catch (NumberFormatException exception) {
			this.commandService.getProvider().sendInternalError(commandEvent, source + " is not a valid number.");
			return null;
		}
	}

}