package com.example.demofle.mongoTemplate;

import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.demofle.config.Config;
import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class WriteEncryptAuto {

  public static void main(String[] args) throws Exception {

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
    MongoOperations mongoTemplate = new MongoTemplate(mongoClient, Config.dbName);

    Customer customer = new Customer();
    customer.setFirstName("Caspar");
    customer.setLastName("Chang");
    customer.setAge(36);
    mongoTemplate.save(customer);

    Document c = new Document()
        .append("firstName", "Esther")
        .append("lastName", "Yu")
        .append("age", 30);
    mongoTemplate.save(c, Config.collName);

  }
}