package org.imanity.framework.command.parameter.impl;

import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
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

	@Autowired
	private CommandService commandService;

	@Override
	public UUID transform(InternalCommandEvent commandEvent, String source) {

		try {
			return UUID.fromString(source);
		} catch (final Exception e) {
			this.commandService.getProvider().sendInternalError(commandEvent, "That UUID could not be parsed.");
		}

		return null;
	}

}