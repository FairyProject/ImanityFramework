package org.imanity.framework.libraries.relocate;

import lombok.AllArgsConstructor;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.LibraryHandler;
import org.imanity.framework.libraries.classloader.IsolatedClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

@AllArgsConstructor
public class RelocateHandler {

    public static final Library[] DEPENDENCIES = new Library[] {Library.ASM, Library.ASM_COMMONS, Library.JAR_RELOCATOR};
    private static final String JAR_RELOCATOR_CLASS = "me.lucko.jarrelocator.JarRelocator";
    private static final String JAR_RELOCATOR_RUN_METHOD = "run";

    private final Constructor<?> jarRelocatorConstructor;
    private final Method jarRelocatorRunMethod;

    public RelocateHandler(LibraryHandler libraryHandler) {
        try {
            // download the required dependencies for remapping
            libraryHandler.downloadLibraries(DEPENDENCIES);
            // get a classloader containing the required dependencies as sources
            IsolatedClassLoader classLoader = libraryHandler.obtainClassLoaderWith(DEPENDENCIES);

            // load the relocator class
            Class<?> jarRelocatorClass = classLoader.loadClass(JAR_RELOCATOR_CLASS);

            // prepare the the reflected constructor & method instances
            this.jarRelocatorConstructor = jarRelocatorClass.getDeclaredConstructor(File.class, File.class, Map.class);
            this.jarRelocatorConstructor.setAccessible(true);

            this.jarRelocatorRunMethod = jarRelocatorClass.getDeclaredMethod(JAR_RELOCATOR_RUN_METHOD);
            this.jarRelocatorRunMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void relocate(Path input, Path output, List<Relocate> relocations) throws Exception {
        Map<String, String> mappings = new HashMap<>();
        for (Relocate relocation : relocations) {
            mappings.put(relocation.getPattern(), relocation.getShadedPattern());
        }

        // create and invoke a new relocator
        Object relocator = this.jarRelocatorConstructor.newInstance(input.toFile(), output.toFile(), mappings);
        this.jarRelocatorRunMethod.invoke(relocator);
    }

}
