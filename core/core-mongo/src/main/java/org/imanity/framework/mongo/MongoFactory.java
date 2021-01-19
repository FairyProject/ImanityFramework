package org.imanity.framework.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MongoFactory {

    private final MongoClient client;
    private final MongoDatabase database;

}
