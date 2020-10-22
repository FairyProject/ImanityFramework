package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class LongParameterType implements ParameterHolder<Long> {

	@Override
	public Class[] type() {
		return new Class[] {Long.class, long.class};
	}

	public Long transform(CommandEvent event, String source) {
		if (source.toLowerCase().contains("e")) {
			event.sendInternalError(source + " is not a valid number.");
			return (null);
		}

		try {
			long parsed = Long.parseLong(source);

			if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
				event.sendInternalError(source + " is not a valid number.");
				return (null);
			}

			return (parsed);
		} catch (NumberFormatException exception) {
			event.sendInternalError(source + " is not a valid number.");
			return (null);
		}
	}

}