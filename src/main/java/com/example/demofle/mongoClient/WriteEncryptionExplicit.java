package com.example.demofle.mongoClient;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.BsonBinary;
import org.bson.BsonString;
import org.bson.Document;
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

        String connectionString = "mongodb://c:c@13.214.135.136:27077";
        String keyVaultNamespace = "encryption.__keyVault";
        String base64KeyId = "87j9MH/FR6e9x2PIXkBiaQ==";

        String path = "master-key.txt";
        byte[] localMasterKey = new byte[96];
        try (FileInputStream fis = new FileInputStream(path)) {
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
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);

        MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("colb");

        // Create the ClientEncryption instance
        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionString))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

        // BsonBinary dataKeyId = clientEncryption.createDataKey("local", new
        // DataKeyOptions());
        BsonBinary dataKeyId = new BsonBinary(Base64.getDecoder().decode(base64KeyId));

        // Explicitly encrypt a field
        BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonString("123456789"),
                new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

        collection.insertOne(new Document("encryptedField", encryptedFieldValue).append("name", "esther"));

        Document doc = collection.find().first();
        System.out.println("Encrypt Document : " + doc.toJson());

        // Explicitly decrypt the field
        System.out.println("Decrypt encryptedField : "
                + clientEncryption.decrypt(new BsonBinary(doc.get("encryptedField", Binary.class).getData())));

        // release resources
        clientEncryption.close();
        mongoClient.close();
    }
}