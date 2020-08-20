package org.imanity.framework.data.store.impl;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.data.type.impl.DocumentDataConverter;

import java.io.File;
import java.io.IOException;

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
    public void load(AbstractData data) {

        File file = new File(this.databaseFolder, data.getUuid().toString() + ".json");
        if (!file.exists()) {
            return;
        }

        try {
            String string = FileUtils.readFileToString(file, Charsets.UTF_8);

            DocumentDataConverter documentData = (DocumentDataConverter) DataConverterType.DOCUMENT.newData();
            documentData.set(string);

            Document document = documentData.get();
            data.loadFromDocument(document, this);
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
            DocumentDataConverter documentData = (DocumentDataConverter) DataConverterType.DOCUMENT.newData();
            documentData.set(this.toDocument(data));

            FileUtils.writeStringToFile(file, documentData.toStringData(false), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while writing json files", ex);
        }
    }
}
