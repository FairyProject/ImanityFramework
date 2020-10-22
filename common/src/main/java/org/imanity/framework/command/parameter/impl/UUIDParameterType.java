package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

import java.util.UUID;

@Component
public class UUIDParameterType implements ParameterHolder<UUID> {

	@Override
	public Class[] type() {
		return new Class[] {UUID.class};
	}

	@Override
	public UUID transform(CommandEvent event, String source) {

		try {
			return UUID.fromString(source);
		} catch (final Exception e) {
			event.sendInternalError("That UUID could not be parsed.");
		}

		return null;
	}

}