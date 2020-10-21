package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class FloatParameterType implements ParameterHolder<Float> {

	@Autowired
	private CommandService commandService;

	@Override
	public Class[] type() {
		return new Class[] {Float.class, float.class};
	}

	public Float transform(InternalCommandEvent event, String source) {
		if (source.toLowerCase().contains("e")) {
			this.commandService.getProvider().sendInternalError(event, source + " is not a valid number.");
			return null;
		}

		try {
			float parsed = Float.parseFloat(source);

			if (Float.isNaN(parsed) || !Float.isFinite(parsed)) {
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