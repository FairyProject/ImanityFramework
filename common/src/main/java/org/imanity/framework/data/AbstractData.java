package org.imanity.framework.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.bson.Document;
import org.imanity.framework.data.store.StoreDatabase;
import java.util.*;

public abstract class AbstractData {

    @JsonProperty
    @Getter
    private UUID uuid;

    public AbstractData(UUID uuid) {
        this.uuid = uuid;
    }

    public AbstractData() {

    }

    public void save() {
        StoreDatabase database = DataHandler.getDatabase(this.getClass());

        database.save(this);
    }

    public Document toDocument() {
        return DataHandler.MAPPER.toJson(this);
    }

}
