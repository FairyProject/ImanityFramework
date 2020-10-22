package org.imanity.framework.boot.console;

import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.imanity.framework.boot.FrameworkBootable;

@RequiredArgsConstructor
public class ImanityConsole extends SimpleTerminalConsole {

    private final FrameworkBootable bootable;

    @Override
    protected boolean isRunning() {
        return !this.bootable.isClosed();
    }

    @Override
    protected void runCommand(String input) {
        this.bootable.issueCommand(input);
    }

    @Override
    protected void shutdown() {
        this.bootable.shutdown();
    }
}
