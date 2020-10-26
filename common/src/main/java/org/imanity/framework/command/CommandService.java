/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.command;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.imanity.framework.annotation.PostInitialize;
import org.imanity.framework.annotation.PreInitialize;
import org.imanity.framework.command.annotation.Command;
import org.imanity.framework.command.annotation.CommandHolder;
import org.imanity.framework.command.annotation.Parameter;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.command.parameter.ParameterMeta;
import org.imanity.framework.plugin.component.ComponentHolder;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.plugin.service.Service;

import java.lang.reflect.Method;
import java.util.*;

@Service(name = "command")
@Getter
public class CommandService {

    public static CommandService INSTANCE;

    private CommandProvider provider;
    private Map<Class<?>, ParameterHolder> parameters;
    private List<CommandMeta> commands;

    @PreInitialize
    public void preInit() {
        this.parameters = new HashMap<>();

        this.commands = new ArrayList<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {CommandHolder.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                Object object = super.newInstance(type);
                registerCommandHolder(object);
                return object;
            }
        });

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {ParameterHolder.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                Object holder = super.newInstance(type);
                registerParameterHolder((ParameterHolder) holder);

                return holder;
            }
        });
    }

    @PostInitialize
    public void init() {
        INSTANCE = this;
    }

    public void registerParameterHolder(ParameterHolder parameterHolder) {
        for (Class type : parameterHolder.type()) {
            this.parameters.put(type, parameterHolder);
        }
    }

    public void registerCommandHolder(Object holder) {
        for (Method method : holder.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                Command command = method.getAnnotation(Command.class);

                final List<ParameterMeta> parameterData = new ArrayList<>();

                // Offset of 1 here for the sender parameter.
                for (int parameterIndex = 1; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
                    java.lang.reflect.Parameter parameter = method.getParameters()[parameterIndex];
                    Parameter parameterAnnotation = parameter.getAnnotation(Parameter.class);

                    if (parameterAnnotation != null) {
                        parameterData.add(new ParameterMeta(parameterAnnotation.name(), parameterAnnotation.wildcard(), parameterAnnotation.defaultValue(), parameterAnnotation.tabCompleteFlags(), parameter.getType()));
                    } else {
                        parameterData.add(new ParameterMeta(parameter.getName(), false, "", new String[] {""}, parameter.getType()));
                    }
                }

                CommandMeta meta = new CommandMeta(command.names(), command.permissionNode(), parameterData, holder, method);
                this.commands.add(meta);
            }
        }
    }

    public void withProvider(CommandProvider provider) {
        this.provider = provider;
    }

    public Object transformParameter(CommandEvent event, String parameter, Class<?> type) {
        if (type == String.class) {
            return parameter;
        }

        ParameterHolder holder = this.parameters.getOrDefault(type, null);
        if (holder == null) {
            throw new IllegalArgumentException("Couldn't find the parameter type " + type.getSimpleName() + ".");
        }

        return holder;
    }

    public List<String> tabCompleteParameters(Object user, String[] parameters, String parameter, Class<?> transformTo, String[] tabCompleteFlags) {
        if (!this.parameters.containsKey(transformTo)) {
            return new ArrayList<>();
        }

        return this.parameters.get(transformTo).tabComplete(user, parameters, ImmutableSet.copyOf(tabCompleteFlags), parameter);
    }

    // Should without the prefix like / or !
    public boolean evalCommand(CommandEvent commandEvent) {
        Object user = commandEvent.getUser();
        String command = commandEvent.getCommand();

        if (command == null || command.length() == 0) {
            return false;
        }

        CommandMeta commandMeta = null;
        String[] arguments = new String[0];

        search:
        for (CommandMeta meta : this.commands) {
            for (String alias : meta.getNames()) {
                String message = command.toLowerCase() + " ";
                String alia = alias.toLowerCase() + " ";

                if (message.startsWith(alia)) {
                    commandMeta = meta;

                    if (message.length() > alia.length()) {
                        if (commandMeta.getParameters().size() == 0) {
                            continue;
                        }
                    }

                    if (command.length() > alias.length() + 1) {
                        arguments = command.substring(alias.length() + 1).split(" ");
                    }

                    break search;
                }
            }
        }

        if (commandMeta == null) {
            return false;
        }

        if (!commandMeta.canAccess(user)) {
            commandEvent.sendNoPermission();
            return false;
        }

        if (!commandEvent.shouldExecute(commandMeta, arguments)) {
            return false;
        }

        try {
            commandMeta.execute(commandEvent, arguments);
        } catch (Throwable throwable) {
            commandEvent.sendError(throwable);
            return false;
        }
        return true;
    }
}
