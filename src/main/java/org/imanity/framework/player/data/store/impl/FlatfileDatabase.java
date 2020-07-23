package org.imanity.framework.player.data.store.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.imanity.framework.Imanity;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.player.data.type.impl.DocumentData;
import org.imanity.framework.player.data.type.impl.JsonData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

public class FlatfileDatabase extends AbstractDatabase {

    private File databaseFolder;

    @Override
    public void init(String name) {
        this.databaseFolder = new File(Imanity.PLUGIN.getDataFolder(), name);
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
            Document jsonObject = new Document();

            jsonObject.put("uuid", playerData.getUuid().toString());
            jsonObject.put("name", playerData.getName());

            for (Data<?> data : playerData.toDataList()) {
                jsonObject.put(data.name(), data.get());
            }

            DocumentData jsonData = (DocumentData) DataType.DOCUMENT.newData();
            jsonData.set(jsonObject);

            FileUtils.writeStringToFile(file, jsonData.toStringData(false), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error while writing json files", ex);
        }
    }
}
