package org.imanity.framework.boot;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.boot.annotation.PostInitialize;
import org.imanity.framework.boot.annotation.PreInitialize;
import org.imanity.framework.boot.console.OptionHandler;
import org.imanity.framework.boot.error.ErrorHandler;
import org.imanity.framework.boot.impl.IndependentCommandExecutor;
import org.imanity.framework.boot.impl.IndependentEventHandler;
import org.imanity.framework.boot.impl.IndependentImanityBridge;
import org.imanity.framework.boot.impl.IndependentPlayerBridge;
import org.imanity.framework.boot.task.AsyncTaskScheduler;
import org.imanity.framework.boot.user.UserInterface;
import org.imanity.framework.boot.console.LoggerOutputStream;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.util.AccessUtil;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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

    public static final Logger LOGGER = LogManager.getLogger();
    public static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat("yyyyMdd-hhmmss");

    private final Map<String, String> configurations;

    private final Class<?> bootableClass;
    private final Set<ErrorHandler> errorHandlers;

    private OptionSet options;
    private final List<OptionHandler> optionHandlers;

    @Nullable
    private UserInterface<?> userInterface;

    private Object bootableObject;
    private PluginClassLoader pluginClassLoader;
    private AsyncTaskScheduler taskScheduler;
    private Yaml yaml;

    private boolean shuttingDown, useJline;

    public FrameworkBootable(Class<?> bootableClass) {
        this.bootableClass = bootableClass;
        this.errorHandlers = Sets.newConcurrentHashSet();

        this.configurations = new HashMap<>();
        this.optionHandlers = new ArrayList<>();
    }

    public FrameworkBootable withUserInterface(UserInterface<?> userInterface) {
        this.userInterface = userInterface;
        return this;
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

            LOGGER.info("Initializing Framework bootable...");

            this.bootableObject = this.bootableClass.newInstance();

            this.call(PreInitialize.class);
            this.initFiles();

            this.pluginClassLoader = new PluginClassLoader(ClassLoader.getSystemClassLoader());
            ImanityCommon.BRIDGE = new IndependentImanityBridge(this);
            ImanityCommon.loadLibraries(); // Pre load libraries since we need it

            this.yaml = new Yaml();
            this.taskScheduler = new AsyncTaskScheduler();

            ImanityCommon.Builder builder = ImanityCommon.builder()
                    .taskScheduler(this.taskScheduler)
                    .eventHandler(new IndependentEventHandler())
                    .commandExecutor(new IndependentCommandExecutor());

            if (this.userInterface != null) {
                builder.playerBridge(new IndependentPlayerBridge(this.userInterface));
            }

            builder.init();

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            this.call(PostInitialize.class);
        } catch (Throwable exception) {
            this.handleError(exception);
        }
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

    private void initConsole() throws IOException {
        System.setOut(new PrintStream(new LoggerOutputStream(LOGGER, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(LOGGER, Level.WARN), true));
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

    public void shutdown() {
        this.shuttingDown = true;

        LOGGER.info("Shutting down...");
    }

}
