package org.imanity.framework.database;

import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.impl.FlatfileDatabase;
import org.imanity.framework.data.store.impl.MongoDatabase;
import org.imanity.framework.data.store.impl.SQLDatabase;

public enum DatabaseType {
    MYSQL(SQLDatabase.class),
    MONGO(MongoDatabase.class),
    FLAT_FILE(FlatfileDatabase.class);

    private Class<? extends StoreDatabase> databaseClass;

    DatabaseType(Class<? extends StoreDatabase> databaseClass) {
        this.databaseClass = databaseClass;
    }

    public StoreDatabase newDatabase() {
        try {
            return this.databaseClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while creating new database", ex);
        }
    }

}
