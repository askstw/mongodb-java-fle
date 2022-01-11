package com.example.demofle.mongoClient;

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

public class QueryEncryptAuto {

        public static void main(final String[] args) throws Exception {

                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
                rootLogger.setLevel(Level.OFF);
        
                String connectionString = "mongodb://c:c@13.214.135.136:27077";
                String keyVaultNamespace = "encryption.__keyVault";
                String base64DataKeyId = "87j9MH/FR6e9x2PIXkBiaQ==";
                String dbName = "test";
                String collName = "colb";
        
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
        
                AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
                        .keyVaultNamespace(keyVaultNamespace)
                        .kmsProviders(kmsProviders)
                        .schemaMap(new HashMap<String, BsonDocument>() {
                            {
                                put(dbName + "." + collName,
                                        // Need a schema that references the new data key
                                        BsonDocument.parse("{"
                                                + "  properties: {"
                                                + "    encryptedField: {"
                                                + "      encrypt: {"
                                                + "        keyId: [{"
                                                + "          \"$binary\": {"
                                                + "            \"base64\": \"" + base64DataKeyId + "\","
                                                + "            \"subType\": \"04\""
                                                + "          }"
                                                + "        }],"
                                                + "        bsonType: \"string\","
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
                        .applyConnectionString(new ConnectionString(connectionString))
                        .build();
        
                MongoClient mongoClient = MongoClients.create(clientSettings);
        
                MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collName);
        
                Bson query1 = Filters.eq("name", "esther");
                Bson query2 = Filters.eq("encryptedField", "123456789");
        
                System.out.println("query1 : " + collection.find(query1).first().toJson());
                System.out.println("query2 : " + collection.find(query2).first().toJson());
        
                // release resources
                mongoClient.close();
            }
}