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

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class QueryEncrypt {

  public static void main(String[] args) throws Exception {

    String connectionString = "mongodb://c:c@13.214.135.136:27077";
    String keyVaultNamespace = "encryption.__keyVault";
    String base64DataKeyId = "87j9MH/FR6e9x2PIXkBiaQ==";
    String dbName = "test";
    String collName = "customer";


    String path = "master-key.txt";
    byte[] localMasterKey= new byte[96];
    try (FileInputStream fis = new FileInputStream(path)) {
        fis.readNBytes(localMasterKey, 0, 96);
    }

    Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {{
       put("local", new HashMap<String, Object>() {{
           put("key", localMasterKey);
       }});
    }};
    
    
    AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
    .keyVaultNamespace(keyVaultNamespace)
    .kmsProviders(kmsProviders)
    .schemaMap(new HashMap<String, BsonDocument>() {{
        put(dbName + "." + collName,
                // Need a schema that references the new data key
                BsonDocument.parse("{"
                        + "  properties: {"
                        + "    age: {"
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
    }}).build();

    MongoClientSettings clientSettings = MongoClientSettings.builder()
      .autoEncryptionSettings(autoEncryptionSettings)
      .applyConnectionString(new ConnectionString(connectionString))
    .build();

    MongoClient mongoClient = MongoClients.create(clientSettings);
    
    MongoOperations mongoTemplate = new MongoTemplate(mongoClient, "test");
    
    System.out.println("1");
    Query query = Query.query(Criteria.where("firstName").is("Caspar"));
    System.out.println("query = " + query.toString());

    System.out.println("2");
    Customer customer = mongoTemplate.findOne(query, Customer.class);
    System.out.println("customer = " + customer.toString());

    System.out.println("3");
    Document c = mongoTemplate.findOne(query, Document.class, "customer");
    System.out.println("document = " + c.toString());

  }
}