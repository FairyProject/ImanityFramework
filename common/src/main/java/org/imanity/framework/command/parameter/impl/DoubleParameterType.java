package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class DoubleParameterType implements ParameterHolder<Double> {

	@Autowired
	private CommandService commandService;

	@Override
	public Class[] type() {
		return new Class[] {Double.class, double.class};
	}

	public Double transform(InternalCommandEvent event, String source) {
		if (source.toLowerCase().contains("e")) {
			this.commandService.getProvider().sendInternalError(event, source + " is not a valid number.");
			return null;
		}

		try {
			double parsed = Double.parseDouble(source);

			if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
				this.commandService.getProvider().sendInternalError(event, source + " is not a valid number.");
				return null;
			}

			return (parsed);
		} catch (NumberFormatException exception) {
			this.commandService.getProvider().sendInternalError(event, source + " is not a valid number.");
			return null;
		}
	}

}