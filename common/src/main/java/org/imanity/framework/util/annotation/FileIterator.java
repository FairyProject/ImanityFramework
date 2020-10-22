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

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * {@code FileIterator} enables iteration over all files in a directory and all its sub 
 * directories.
 * <p>
 * Usage:
 * <pre>
 * FileIterator iter = new FileIterator(new File("./src"));
 * File f;
 * while ((f = iter.next()) != null) {
 *     // do something with f
 *     assert f == iter.getCurrent();
 * }
 * </pre>
 * 
 * @author <a href="mailto:rmuller@xiam.nl">Ronald K. Muller</a>
 * @since annotation-detector 3.0.0
 */
final class FileIterator {

    private final Deque<File> stack = new LinkedList<File>();
    private int rootCount;
    private File current;
    
    /**
     * Create a new {@code FileIterator} using the specified 'filesOrDirectories' as root.
     * <p>
     * If 'filesOrDirectories' contains a file, the iterator just returns that single file. 
     * If 'filesOrDirectories' contains a directory, all files in that directory 
     * and its sub directories are returned (depth first).
     * 
     * @param filesOrDirectories Zero or more {@link File} objects, which are iterated
     * in the specified order (depth first)
     */
    FileIterator(final File... filesOrDirectories) {
        addReverse(filesOrDirectories);
        rootCount = stack.size();
    }

    /**
     * Return the last returned file or {@code null} if no more files are available.
     * 
     * @see #next()
     */
    public File getFile() {
        return current;
    }
    
    /**
     * Return {@code true} if the current file is one of the files originally
     * specified as one of the constructor file parameters, i.e. is a root file
     * or directory.
     */
    public boolean isRootFile() {
        if (current == null) {
            throw new NoSuchElementException();
        }
        return stack.size() < rootCount;
    }
    
    /**
     * Return the next {@link File} object or {@code null} if no more files are
     * available.
     * 
     * @see #getFile()
     */
    public File next() throws IOException {
        if (stack.isEmpty()) {
            current = null;
            return null;
        } else {
            current = stack.removeLast();
            if (current.isDirectory()) {
                if (stack.size() < rootCount) {
                    rootCount = stack.size();
                }
                addReverse(current.listFiles());
                return next();
            } else {
                return current;
            }
        }
    }

    /**
     * Add the specified files in reverse order.
     */
    private void addReverse(final File[] files) {
        for (int i = files.length - 1; i >= 0; --i) {
            stack.add(files[i]);
        }
    }

}
