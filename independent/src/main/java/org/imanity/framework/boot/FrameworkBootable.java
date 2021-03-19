/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.boot;

import com.google.common.base.Preconditions;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.Getter;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.*;
import org.imanity.framework.boot.console.ForwardLogHandler;
import org.imanity.framework.boot.console.ImanityConsole;
import org.imanity.framework.boot.console.OptionHandler;
import org.imanity.framework.boot.console.command.ConsoleCommandEvent;
import org.imanity.framework.boot.console.command.ConsolePresenceProvider;
import org.imanity.framework.boot.enums.ProgramStatus;
import org.imanity.framework.boot.error.ErrorHandler;
import org.imanity.framework.boot.impl.IndependentCommandExecutor;
import org.imanity.framework.boot.impl.IndependentEventHandler;
import org.imanity.framework.boot.impl.IndependentImanityPlatform;
import org.imanity.framework.boot.task.AsyncTaskScheduler;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.plugin.PluginClassLoader;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.CC;
import org.imanity.framework.util.Utility;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * TODO:
 * - Independent Event Handler
 *
 */
@Getter
public final class FrameworkBootable {

    private static FrameworkBootable INSTANCE;

    public static Logger LOGGER;
    public static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat("yyyyMdd-hhmmss");

    private final Map<String, String> configurations;

    private final Class<?> bootableClass;
    private final Set<ErrorHandler> errorHandlers;

    private OptionSet options;
    private final List<OptionHandler> optionHandlers;

    private Object bootableObject;
    private PluginClassLoader pluginClassLoader;
    private AsyncTaskScheduler taskScheduler;
    private Yaml yaml;

    private volatile ProgramStatus status;

    @Autowired
    private CommandService commandService;

    @Autowired
    private BeanContext beanContext;

    @Bean
    public static FrameworkBootable bean() {
        return FrameworkBootable.INSTANCE;
    }

    @Bean
    public static Object beanBootable() {
        return FrameworkBootable.INSTANCE.getBootableObject();
    }

    public FrameworkBootable(Class<?> bootableClass) {
        INSTANCE = this;

        this.bootableClass = bootableClass;
        this.errorHandlers = new HashSet<>();

        this.configurations = new HashMap<>();
        this.optionHandlers = new ArrayList<>();

        this.status = ProgramStatus.BOOTING;

        LOGGER = LogManager.getLogger(this.bootableClass);
    }

    public FrameworkBootable withOptionHandler(OptionHandler optionHandler) {
        this.optionHandlers.add(optionHandler);
        return this;
    }

    public FrameworkBootable withConfigValue(String key, String value) {
        this.configurations.put(key, value);
        return this;
    }

    public void boot(String[] args) {
        try {

            this.initArguments(args);
            this.initConsole();

            LOGGER.info("\n" + CC.CHAT_BAR + "\n" +
                               "§b██╗███╗░░░███╗░█████╗░███╗░░██╗██╗████████╗██╗░░░██╗\n" +
                                 "██║████╗░████║██╔══██╗████╗░██║██║╚══██╔══╝╚██╗░██╔╝\n" +
                                 "██║██╔████╔██║███████║██╔██╗██║██║░░░██║░░░░╚████╔╝░\n" +
                                 "██║██║╚██╔╝██║██╔══██║██║╚████║██║░░░██║░░░░░╚██╔╝░░\n" +
                                 "██║██║░╚═╝░██║██║░░██║██║░╚███║██║░░░██║░░░░░░██║░░░\n" +
                                 "╚═╝╚═╝░░░░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝╚═╝░░░╚═╝░░░░░░╚═╝░░░\n" +
                                 CC.GRAY + CC.STRIKE_THROUGH + "========== " + CC.WHITE + "Made with " + CC.PINK + "Love <3 " + CC.GRAY + CC.STRIKE_THROUGH + "==========");
            LOGGER.info("Initializing Framework bootable...");

            this.bootableObject = this.bootableClass.newInstance();

            this.call(PreBooting.class);
            this.initFiles();

            this.pluginClassLoader = new PluginClassLoader(ClassLoader.getSystemClassLoader());
            ImanityCommon.PLATFORM = new IndependentImanityPlatform(this);
            FrameworkMisc.PLATFORM = ImanityCommon.PLATFORM;
            ImanityCommon.loadLibraries(); // Pre load libraries since we need it

            this.yaml = new Yaml();
            this.taskScheduler = new AsyncTaskScheduler();

            ImanityCommon.Builder builder = ImanityCommon.builder()
                    .taskScheduler(this.taskScheduler)
                    .eventHandler(new IndependentEventHandler())
                    .commandExecutor(new IndependentCommandExecutor());
            builder.init();

            this.beanContext.scanClasses("bootable", ClassLoader.getSystemClassLoader(), this.beanContext.findClassPaths(this.bootableClass));

            ImanityCommon.getBean(CommandService.class).registerDefaultPresenceProvider(new ConsolePresenceProvider());

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Shutdown Thread"));

            this.status = ProgramStatus.RUNNING;
            this.call(PostBooting.class);
        } catch (Throwable exception) {
            this.handleError(exception);
        }
    }

    public void issueCommand(String command) {
        if (this.commandService == null) {
            LOGGER.warn(CC.RED + "You couldn't do this yet!");
            return;
        }

        this.commandService.evalCommand(new ConsoleCommandEvent(command));
    }

    private void call(Class<? extends Annotation> annotation) throws ReflectiveOperationException {
        Preconditions.checkNotNull(this.bootableObject, "The Bootable Object is null!");

        for (Method method : this.bootableClass.getDeclaredMethods()) {
            if (method.getAnnotation(annotation) == null) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            AccessUtil.setAccessible(method);
            if (method.getParameterCount() == 0) {
                method.invoke(this.bootableObject);
            } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == FrameworkBootable.class) {
                method.invoke(bootableObject, this);
            } else {
                this.handleError(new IllegalArgumentException("The method " + method.getName() + " contain annotation " + annotation.getSimpleName() + " but doesn't have matches parameters!"));
            }
        }
    }

    public void handleError(Throwable exception) {
        boolean crash = true;

        for (ErrorHandler errorHandler : this.errorHandlers) {
            for (Class<? extends Throwable> type : errorHandler.types()) {
                if (type.isInstance(exception)) {
                    boolean result = errorHandler.handle(exception);
                    if (!result) {
                        crash = false;
                    }
                }
            }
        }

        if (crash) {
            LOGGER.error("Unexpected error occurs, It didn't get handled so crashes the program, please read crash file", exception);

            try {
                File file = this.getCrashLogFile();
                if (!file.exists()) {
                    file.createNewFile();
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    exception.printStackTrace(writer);
                    writer.flush();
                }
            } catch (IOException ex) {
                LOGGER.error("An error occurs while creating crash log", ex);
            }

            System.exit(0);
        } else {
            LOGGER.error("Unexpected error occurs", exception);
        }
    }

    private void initArguments(String[] args) {

        OptionParser optionParser = new OptionParser();

        for (OptionHandler optionHandler : this.optionHandlers) {
            String[] options = optionHandler.option();
            String description = optionHandler.description();
            if (description == null) {
                description = "";
            }

            if (options.length == 1) {
                optionParser.accepts(options[0]);
            } else {
                optionParser.acceptsAll(Arrays.asList(options), description);
            }
        }

        try {
            this.options = optionParser.parse(args);
        } catch (OptionException ex) {
            ex.printStackTrace();
        }

        if (this.options != null) {
            for (OptionHandler optionHandler : this.optionHandlers) {
                for (String option : optionHandler.option()) {
                    if (options.has(option)) {
                        optionHandler.call(options.valueOf(option));
                    }
                }
            }
        }
    }

    public boolean has(String key) {
        return this.configurations.containsKey(key);
    }

    public String get(String key, String orDefault) {
        String value = this.configurations.getOrDefault(key, null);
        return value != null ? value : orDefault;
    }

    public boolean getBoolean(String key, boolean orDefault) {
        String value = this.get(key, null);
        return value != null ? Boolean.parseBoolean(value) : orDefault;
    }

    public int getInteger(String key, int orDefault) {
        String value = this.get(key, null);
        return value != null ? Integer.parseInt(value) : orDefault;
    }

    public long getLong(String key, long orDefault) {
        String value = this.get(key, null);
        return value != null ? Long.parseLong(value) : orDefault;
    }

    private void initConsole() {
        Thread thread = new Thread("Console Thread") {
            @Override
            public void run() {
                new ImanityConsole(FrameworkBootable.this).start();
            }
        };

        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
        global.setUseParentHandlers(false);
        for (java.util.logging.Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new ForwardLogHandler());

        final Logger logger = LogManager.getRootLogger();

        System.setOut(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream());
        System.setErr(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.WARN).buildPrintStream());

        thread.setDaemon(true);
        thread.start();

        LOGGER.info("Console initialized.");
    }

    private void initFiles() {
        File logsFolder = new File("logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
    }

    public File getCrashLogFile() {
        File folder = new File("crashes");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Date date = new Date(System.currentTimeMillis());
        return new File(folder, "crash-" + LOG_FILE_FORMAT.format(date) + ".log");
    }

    public boolean isShuttingDown() {
        return this.status == ProgramStatus.SHUTTING_DOWN;
    }

    public boolean isClosed() {
        return this.status == ProgramStatus.CLOSED;
    }

    private final Object shutdownLock = new Object();
    private boolean closed;

    public void shutdown() {

        synchronized (this.shutdownLock) {
            if (this.closed) {
                return;
            }

            closed = true;
        }

        if (this.status == ProgramStatus.SHUTTING_DOWN || this.status == ProgramStatus.CLOSED) {
            return;
        }

        try {
            this.status = ProgramStatus.SHUTTING_DOWN;

            LOGGER.info("Shutting down...");
            Utility.tryCatch(() -> this.call(PreDestroy.class));

            ImanityCommon.shutdown();

            Utility.tryCatch(() -> this.call(PostDestroy.class));
            this.status = ProgramStatus.CLOSED;

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        System.exit(0);

        try {
            TerminalConsoleAppender.close();
        } catch (IOException e) {
            // IGNORE
        }

    }
}
