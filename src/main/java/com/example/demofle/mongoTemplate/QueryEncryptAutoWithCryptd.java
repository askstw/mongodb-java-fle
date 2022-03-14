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

public class QueryEncryptAutoWithCryptd {

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

    //List<String> spawnArgs = new ArrayList<String>();
    //spawnArgs.add("--port=30000");

    Map<String, Object> extraOpts = new HashMap<String, Object>();
    extraOpts.put("mongocryptdURI", "mongodb://localhost:30000");

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

                + "    ,name: {"
                + "      encrypt: {"
                + "        keyId: [{"
                + "          \"$binary\": {"
                + "            \"base64\": \"" + Config.base64DataKeyId + "\","
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
        })
        .extraOptions(extraOpts)
        .build();

    MongoClientSettings clientSettings = MongoClientSettings.builder()
        .autoEncryptionSettings(autoEncryptionSettings)
        .applyConnectionString(new ConnectionString(Config.connectionString))
        .build();

    MongoClient mongoClient = MongoClients.create(clientSettings);
    MongoOperations mongoTemplate = new MongoTemplate(mongoClient, Config.dbName);

    //查詢加密欄位 name
    Query query1 = Query.query(Criteria.where("name").is("張大哥"));
    System.out.println("query1 = " + query1.toString());

    Customer customer = mongoTemplate.findOne(query1, Customer.class);
    if(null != customer)
      System.out.println("customer1 = " + customer.toString());

    //查詢加密欄位 age
    Query query2 = Query.query(Criteria.where("age").is(30));
    System.out.println("query2 = " + query2.toString());

    Document c = mongoTemplate.findOne(query2, Document.class, Config.collName);
    if(null != c)
      System.out.println("document2 = " + c.toString());

  }
}