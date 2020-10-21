package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class LongParameterType implements ParameterHolder<Long> {

	@Autowired
	private CommandService commandService;

	@Override
	public Class[] type() {
		return new Class[] {Long.class, long.class};
	}

	public Long transform(InternalCommandEvent commandEvent, String source) {
		if (source.toLowerCase().contains("e")) {
			this.commandService.getProvider().sendInternalError(commandEvent, source + " is not a valid number.");
			return (null);
		}

		try {
			long parsed = Long.parseLong(source);

			if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
				this.commandService.getProvider().sendInternalError(commandEvent, source + " is not a valid number.");
				return (null);
			}

			return (parsed);
		} catch (NumberFormatException exception) {
			this.commandService.getProvider().sendInternalError(commandEvent, source + " is not a valid number.");
			return (null);
		}
	}

}