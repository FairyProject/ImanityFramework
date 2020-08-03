package org.imanity.framework.command;

import org.imanity.framework.redis.server.ImanityServer;

public interface ICommandExecutor {

    void execute(String command, String context, String executor, ImanityServer server);

}
