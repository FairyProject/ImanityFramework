package org.imanity.framework.data.store.impl;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;

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
            return Document.parse(FileUtils.readFileToString(file, Charsets.UTF_8));
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

            FileUtils.writeStringToFile(file, document.toJson(), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while writing json files", ex);
        }
    }
}
