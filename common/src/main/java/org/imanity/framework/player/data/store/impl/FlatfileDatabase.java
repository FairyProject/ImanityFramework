package org.imanity.framework.player.data.store.impl;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.player.data.type.impl.DocumentData;

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
    public void load(PlayerData playerData) {

        File file = new File(this.databaseFolder, playerData.getUuid().toString() + ".json");
        if (!file.exists()) {
            return;
        }

        try {
            String string = FileUtils.readFileToString(file, Charsets.UTF_8);

            DocumentData documentData = (DocumentData) DataType.DOCUMENT.newData();
            documentData.set(string);

            Document document = documentData.get();
            playerData.loadFromDocument(document);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while reading json files", ex);
        }

    }

    @Override
    public void save(PlayerData playerData) {
        File file = new File(this.databaseFolder, playerData.getUuid().toString() + ".json");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException("Unexpected error while creating new data file " + file.getAbsolutePath(), ex);
            }
        }

        try {
            DocumentData documentData = (DocumentData) DataType.DOCUMENT.newData();
            documentData.set(playerData.toDocument());

            FileUtils.writeStringToFile(file, documentData.toStringData(false), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while writing json files", ex);
        }
    }
}
