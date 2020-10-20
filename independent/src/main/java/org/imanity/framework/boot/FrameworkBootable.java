package org.imanity.framework.boot;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.boot.annotation.PostInitialize;
import org.imanity.framework.boot.annotation.PreInitialize;
import org.imanity.framework.boot.error.ErrorHandler;
import org.imanity.framework.boot.impl.IndependentCommandExecutor;
import org.imanity.framework.boot.impl.IndependentEventHandler;
import org.imanity.framework.boot.impl.IndependentImanityBridge;
import org.imanity.framework.boot.impl.IndependentPlayerBridge;
import org.imanity.framework.boot.task.AsyncTaskScheduler;
import org.imanity.framework.boot.user.UserInterface;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.util.AccessUtil;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 *
 * TODO:
 * - Independent Event Handler
 *
 */
@Getter
public class FrameworkBootable {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat("yyyyMdd-hhmmss");

    private final Class<?> bootableClass;
    private final Set<ErrorHandler> errorHandlers;

    @Nullable
    private UserInterface<?> userInterface;

    private Object bootableObject;
    private PluginClassLoader pluginClassLoader;
    private AsyncTaskScheduler taskScheduler;
    private Yaml yaml;

    private boolean shuttingDown;

    public FrameworkBootable(Class<?> bootableClass) {
        this.bootableClass = bootableClass;
        this.errorHandlers = Sets.newConcurrentHashSet();
    }

    public FrameworkBootable withUserInterface(UserInterface<?> userInterface) {
        this.userInterface = userInterface;
        return this;
    }

    public void boot() {
        try {
            this.bootableObject = this.bootableClass.newInstance();

            this.call(PreInitialize.class);
            this.taskScheduler = new AsyncTaskScheduler();
            this.pluginClassLoader = new PluginClassLoader(this.getBootableClass().getClassLoader());
            this.yaml = new Yaml();

            ImanityCommon.Builder builder = ImanityCommon.builder()
                    .taskScheduler(this.taskScheduler)
                    .eventHandler(new IndependentEventHandler())
                    .commandExecutor(new IndependentCommandExecutor())
                    .bridge(new IndependentImanityBridge(this));

            if (this.userInterface != null) {
                builder.playerBridge(new IndependentPlayerBridge(this.userInterface));
            }

            builder.init();

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
                try (PrintWriter writer = new PrintWriter(file)) {
                    exception.printStackTrace(writer);
                    writer.flush();
                }
            } catch (IOException ex) {
                LOGGER.error("An error occurs while creating crash log", ex);
            }
        } else {
            LOGGER.error("Unexpected error occurs", exception);
        }
    }

    public File getCrashLogFile() {
        Date date = new Date(System.currentTimeMillis());
        return new File("logs", "crash-" + LOG_FILE_FORMAT.format(date) + ".log");
    }

}
