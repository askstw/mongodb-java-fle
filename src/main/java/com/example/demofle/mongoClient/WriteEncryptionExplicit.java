package com.example.demofle.mongoClient;

import com.example.demofle.config.Config;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.BsonBinary;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

//ClientSideEncryptionExplicitEncryptionAndDecryptionTour

public class WriteEncryptionExplicit {

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

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(Config.connectionString))
                .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);
        MongoCollection<Document> collection = mongoClient.getDatabase(Config.dbName).getCollection(Config.collName);

        // Create the ClientEncryption instance
        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(Config.connectionString))
                        .build())
                .keyVaultNamespace(Config.keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

        BsonBinary dataKeyId = new BsonBinary(Base64.getDecoder().decode(Config.base64DataKeyId));

        // Explicitly encrypt a field
        BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonInt32(4),
                new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

        collection.insertOne(new Document("firstName", "Curry")
                .append("lastName", "Chang")
                .append("age", encryptedFieldValue));

        Bson query1 = Filters.eq("firstName", "Curry");
        Document doc = collection.find(query1).first();
        System.out.println("Encrypt Document : " + doc.toJson());

        // Explicitly decrypt the field
        System.out.println("Decrypt Age : "
                + clientEncryption.decrypt(new BsonBinary(doc.get("age", Binary.class).getData())));

        // release resources
        clientEncryption.close();
        mongoClient.close();
    }
}