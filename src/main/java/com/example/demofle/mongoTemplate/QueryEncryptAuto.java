package com.example.demofle.mongoTemplate;

import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.demofle.config.Config;
import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class QueryEncryptAuto {

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

    Query query1 = Query.query(Criteria.where("firstName").is("Caspar"));
    System.out.println("query1 = " + query1.toString());

    Customer customer = mongoTemplate.findOne(query1, Customer.class);
    System.out.println("customer = " + customer.toString());


    Query query2 = Query.query(Criteria.where("age").is(30));
    System.out.println("query2 = " + query2.toString());

    Document c = mongoTemplate.findOne(query2, Document.class, Config.collName);
    System.out.println("document = " + c.toString());

  }
}