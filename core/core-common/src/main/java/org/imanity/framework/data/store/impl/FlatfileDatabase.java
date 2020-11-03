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

package org.imanity.framework.data.store.impl;

import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.imanity.framework.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FlatfileDatabase extends AbstractDatabase {

    private File databaseFolder;

    @Override
    public void init(String name) {
        this.databaseFolder = new File(ImanityCommon.BRIDGE.getDataFolder(), name);
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }
    }

    @Override
    public Document load(UUID uuid) {

        File file = new File(this.databaseFolder, uuid.toString() + ".json");
        if (!file.exists()) {
            return null;
        }

        try {
            return Document.parse(IO.readFile(file));
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while reading json files", ex);
        }

    }

    @Override
    public void save(AbstractData data) {
        File file = new File(this.databaseFolder, data.getUuid().toString() + ".json");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException("Unexpected error while creating new data file " + file.getAbsolutePath(), ex);
            }
        }

        try {
            Document document = data.toDocument();

            IO.writeFile(file, document.toJson());
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while writing json files", ex);
        }
    }
}
