package com.example.demofle.mongoClient;

import com.example.demofle.config.Config;
import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

//ClientSideEncryptionAutoEncryptionSettingsTour

public class WriteEncryptionAuto {
    public static void main(final String[] args) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        byte[] localMasterKey = new byte[96];
        try (FileInputStream fis = new FileInputStream(Config.masterKeyFile)) {
            fis.readNBytes(localMasterKey, 0, 96);
        }

        Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {
            {
                put("local", new HashMap<String, Object>() {
                    {
                        put("key", localMasterKey);
                    }
                });
            }
        };

        AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
                .keyVaultNamespace(Config.keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .schemaMap(new HashMap<String, BsonDocument>() {
                    {
                        put(Config.nameSpace,
                                // Need a schema that references the new data key
                                BsonDocument.parse("{"
                                        + "  properties: {"
                                        + "    age: {"
                                        + "      encrypt: {"
                                        + "        keyId: [{"
                                        + "          \"$binary\": {"
                                        + "            \"base64\": \"" + Config.base64DataKeyId + "\","
                                        + "            \"subType\": \"04\""
                                        + "          }"
                                        + "        }],"
                                        + "        bsonType: \"int\","
                                        + "        algorithm: \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\""
                                        + "      }"
                                        + "    }"
                                        + "  },"
                                        + "  \"bsonType\": \"object\""
                                        + "}"));
                    }
                }).build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .autoEncryptionSettings(autoEncryptionSettings)
                .applyConnectionString(new ConnectionString(Config.connectionString))
                .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);
        MongoCollection<Document> collection = mongoClient.getDatabase(Config.dbName).getCollection(Config.collName);

        collection.insertOne(new Document("firstName", "Caspar")
                .append("lastName", "Chang")
                .append("age", 36));

        collection.insertOne(new Document("firstName", "Esther")
                .append("lastName", "Yu")
                .append("age", 30));

        Bson query1 = Filters.eq("firstName", "Caspar");
        Bson query2 = Filters.eq("age", 30);

        System.out.println("query1 : " + collection.find(query1).first().toJson());
        System.out.println("query2 : " + collection.find(query2).first().toJson());

        // release resources
        mongoClient.close();
    }
}