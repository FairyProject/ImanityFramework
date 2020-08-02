package org.imanity.framework.exception;

public class OptionNotEnabledException extends RuntimeException {

    public OptionNotEnabledException(String name, String configName) {
        super("The Option " + name + " in " + configName + ".yml isn't enabled! So specific feature wouldn't works!");
    }

}
