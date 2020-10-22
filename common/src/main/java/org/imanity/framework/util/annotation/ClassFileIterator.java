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
package org.imanity.framework.util.annotation;

import java.io.*;
import java.util.zip.ZipFile;

/**
 * {@code ClassFileIterator} is used to iterate over all Java ClassFile files available within
 * a specific context.
 * <p>
 * For every Java ClassFile ({@code .class}) an {@link InputStream} is returned.
 *
 * @author <a href="mailto:rmuller@xiam.nl">Ronald K. Muller</a>
 * @since annotation-detector 3.0.0
 */
public final class ClassFileIterator extends ResourceIterator {

    private final FileIterator fileIterator;
    private final String[] pkgNameFilter;
    private ZipFileIterator zipIterator;

    /**
     * Create a new {@code ClassFileIterator} returning all Java ClassFile files available
     * from the class path ({@code System.getProperty("java.class.path")}).
     */
    ClassFileIterator() {
        this(classPath(), null);
    }

    /**
     * Create a new {@code ClassFileIterator} returning all Java ClassFile files available
     * from the specified files and/or directories, including sub directories.
     * <p>
     * If the (optional) package filter is defined, only class files staring with one of the
     * defined package names are returned.
     * NOTE: package names must be defined in the native format (using '/' instead of '.').
     */
    public ClassFileIterator(final File[] filesOrDirectories, final String[] pkgNameFilter) {
        this.fileIterator = new FileIterator(filesOrDirectories);
        this.pkgNameFilter = pkgNameFilter;
    }

    /**
     * Return the name of the Java ClassFile returned from the last call to {@link #next()}.
     * The name is either the path name of a file or the name of an ZIP/JAR file entry.
     */
    public String getName() {
        // Both getPath() and getName() are very light weight method calls
        return zipIterator == null ?
            fileIterator.getFile().getPath() :
            zipIterator.getEntry().getName();
    }

    @Override
    public InputStream next() throws IOException {
        while (true) {
            if (zipIterator == null) {
                final File file = fileIterator.next();
                // not all specified Files exists!
                if (file == null || !file.isFile()) {
                    return null;
                } else {
                    final String name = file.getName();
                    if (name.endsWith(".class")) {
                        return new FileInputStream(file);
                    } else if (fileIterator.isRootFile() &&
                        (endsWithIgnoreCase(name, ".jar") || isZipFile(file))) {
                        zipIterator = new ZipFileIterator(new ZipFile(file), pkgNameFilter);
                    } // else just ignore
                }
            } else {
                final InputStream is = zipIterator.next();
                if (is == null) {
                    zipIterator = null;
                } else {
                    return is;
                }
            }
        }
    }

    // private

    private boolean isZipFile(final File file) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(file));
            final int n = in.readInt();
            return n == 0x504b0304;
        } catch (IOException ex) {
            // silently ignore read exceptions
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns the class path of the current JVM instance as an array of {@link File} objects.
     */
    private static File[] classPath() {
        final String[] fileNames =
            System.getProperty("java.class.path").split(File.pathSeparator);
        final File[] files = new File[fileNames.length];
        for (int i = 0; i < files.length; ++i) {
            files[i] = new File(fileNames[i]);
        }
        return files;
    }

    private static boolean endsWithIgnoreCase(final String value, final String suffix) {
        final int n = suffix.length();
        return value.regionMatches(true, value.length() - n, suffix, 0, n);
    }

}
