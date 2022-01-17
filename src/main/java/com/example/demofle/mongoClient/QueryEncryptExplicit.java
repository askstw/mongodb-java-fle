package com.example.demofle.mongoClient;

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

public class QueryEncryptExplicit {

        public static void main(final String[] args) throws Exception {

                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
                rootLogger.setLevel(Level.OFF);

                String connectionString = "mongodb://c:c@13.214.135.136:27077";
                String keyVaultNamespace = "encryption.__keyVault";
                String base64DataKeyId = "87j9MH/FR6e9x2PIXkBiaQ==";
                String dbName = "test";
                String collName = "customer";

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

                MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collName);

                // Create the ClientEncryption instance
                ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                                                .applyConnectionString(new ConnectionString(connectionString))
                                                .build())
                                .keyVaultNamespace(keyVaultNamespace)
                                .kmsProviders(kmsProviders)
                                .build();

                ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

                Bson query1 = Filters.eq("firstName", "Curry");
                Document doc1 = collection.find(query1).first();
                System.out.println("Encrypt Document1 : " + doc1.toJson());

                // Explicitly decrypt the field
                System.out.println("Decrypt Document1 Age : "
                                + clientEncryption.decrypt(new BsonBinary(doc1.get("age", Binary.class).getData())));

                
                BsonBinary dataKeyId = new BsonBinary(Base64.getDecoder().decode(base64DataKeyId));
                BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonInt32(4),
                                new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));
                Bson query2 = Filters.eq("age", encryptedFieldValue);
                Document doc2 = collection.find(query2).first();
                System.out.println("Encrypt Document2 : " + doc2.toJson());
                System.out.println("Decrypt Document2 Age : "
                                + clientEncryption.decrypt(new BsonBinary(doc2.get("age", Binary.class).getData())));

                // release resources
                clientEncryption.close();
                mongoClient.close();
        }
}