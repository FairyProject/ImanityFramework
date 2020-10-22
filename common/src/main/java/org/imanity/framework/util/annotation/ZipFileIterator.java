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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * {@code ZipFileIterator} is used to iterate over all entries in a given {@code zip} or
 * {@code jar} file and returning the {@link InputStream} of these entries.
 * <p>
 * It is possible to specify an (optional) entry name filter.
 * <p>
 * The most efficient way of iterating is used, see benchmark in test classes.
 *
 * @author <a href="mailto:rmuller@xiam.nl">Ronald K. Muller</a>
 * @since annotation-detector 3.0.0
 */
final class ZipFileIterator {

    private final ZipFile zipFile;
    private final String[] entryNameFilter;
    private final Enumeration<? extends ZipEntry> entries;

    private ZipEntry current;

    /**
     * Create a new {@code ZipFileIterator} instance.
     * 
     * @param zipFile The ZIP file used to iterate over all entries
     * @param entryNameFilter (optional) file name filter. Only entry names starting with
     * one of the specified names in the filter are returned
     */
    ZipFileIterator(final ZipFile zipFile, final String[] entryNameFilter) throws IOException {
        this.zipFile = zipFile;
        this.entryNameFilter = entryNameFilter;

        this.entries = zipFile.entries();
    }

    public ZipEntry getEntry() {
        return current;
    }

    @SuppressWarnings("emptyblock")
    public InputStream next() throws IOException {
        while (entries.hasMoreElements()) {
            current = entries.nextElement();
            if (accept(current)) {
                return zipFile.getInputStream(current);
            }
        }
        // no more entries in this ZipFile, so close ZipFile
        try {
            // zipFile is never null here
            zipFile.close();
        } catch (IOException ex) {
            // suppress IOException, otherwise close() is called twice
        }
        return null;
    }

    private boolean accept(final ZipEntry entry) {
        if (entry.isDirectory()) {
            return false;
        }
        if (entryNameFilter == null) {
            return true;
        }
        for (final String filter : entryNameFilter) {
            if (entry.getName().startsWith(filter)) {
                return true;
            }
        }
        return false;
    }

}
