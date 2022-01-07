package com.example.demofle.templ;

import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class WriteEncrypt {

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
    
    Customer customer = new Customer(); 
    customer.setFirstName("Caspar"); 
    customer.setLastName("Chang"); 
    customer.setAge("30"); 
    mongoTemplate.save(customer);

    Document c = new Document()
      .append("firstName", "Esther")
      .append("lastName", "Yu")
      .append("age", "20");
    
    mongoTemplate.save(c, "customer");
  }
}