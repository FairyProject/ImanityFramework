package org.imanity.framework.player.data.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.imanity.framework.player.data.PlayerData;

import java.lang.reflect.Field;

public interface Data<T> {

    String name();

    T get();

    void set(Object object);

    void setName(String name);

    SQLColumnType columnType();

    String toStringData(boolean sql);

    Object toFieldObject(Field field);

    Class<T> getType();

}
