package org.imanity.framework.exception;

public class ServiceUnknownDependencyException extends IllegalArgumentException {

    public ServiceUnknownDependencyException(String name, String dependency) {
        super("The service " + name + " doesn't have the valid dependency " + dependency + "!");
    }


}
