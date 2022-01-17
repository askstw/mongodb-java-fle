package com.example.demofle.mongoTemplate;

import org.bson.BsonBinary;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

public class WriteEncryptExplicit {

  public static void main(String[] args) throws Exception {

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

    System.out.println("0");

    MongoClient mongoClient = MongoClients.create(clientSettings);

    MongoOperations mongoTemplate = new MongoTemplate(mongoClient, dbName);

    ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .build())
        .keyVaultNamespace(keyVaultNamespace)
        .kmsProviders(kmsProviders)
        .build();

    ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

    BsonBinary dataKeyId = new BsonBinary(Base64.getDecoder().decode(base64DataKeyId));
    BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonInt32(4),
        new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

    Document c = new Document()
        .append("firstName", "Curry")
        .append("lastName", "Chang")
        .append("age", encryptedFieldValue);
    mongoTemplate.save(c, collName);

    Query query1 = Query.query(Criteria.where("age").is(encryptedFieldValue));
    System.out.println("query = " + query1.toString());

    Document c1 = mongoTemplate.findOne(query1, Document.class, collName);
    System.out.println("Encrypt Document : " + c1.toJson());

    System.out.println("Decrypt encryptedField : " + clientEncryption
        .decrypt(new BsonBinary(c1.get("age", Binary.class).getData())));

    Query query2 = Query.query(Criteria.where("firstName").is("Curry"));
    System.out.println("query = " + query2.toString());

    Document c2 = mongoTemplate.findOne(query2, Document.class, collName);
    System.out.println("Encrypt Document : " + c2.toJson());

    System.out.println("Decrypt encryptedField : " + clientEncryption
        .decrypt(new BsonBinary(c2.get("age", Binary.class).getData())));

  }
}