package com.example.demofle.mongoTemplate;

import org.bson.BsonBinary;
import org.bson.BsonInt32;
import org.bson.BsonString;
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

import com.example.demofle.config.Config;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

public class QueryEncryptExplicit {

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

    MongoClientSettings clientSettings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(Config.connectionString))
        .build();

    System.out.println("0");

    MongoClient mongoClient = MongoClients.create(clientSettings);

    MongoOperations mongoTemplate = new MongoTemplate(mongoClient, Config.dbName);

    ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(Config.connectionString))
            .build())
        .keyVaultNamespace(Config.keyVaultNamespace)
        .kmsProviders(kmsProviders)
        .build();

    ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

    BsonBinary dataKeyId = new BsonBinary(Base64.getDecoder().decode(Config.base64DataKeyId));
    
    BsonBinary encryptedFieldAgeValue = clientEncryption.encrypt(new BsonInt32(4),
        new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

    BsonBinary encryptedFieldNameValue = clientEncryption.encrypt(new BsonString("大吉"),
        new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

    //查詢年紀等於加密過後的4
    Query query1 = Query.query(Criteria.where("age").is(encryptedFieldAgeValue));
    System.out.println("query = " + query1.toString());

    Document c1 = mongoTemplate.findOne(query1, Document.class, Config.collName);
    System.out.println("Encrypt Document : " + c1.toJson());

    System.out.println("Decrypt encryptedField : " + clientEncryption
        .decrypt(new BsonBinary(c1.get("age", Binary.class).getData())));

    //查詢名字等於加密過後的大吉
    Query query2 = Query.query(Criteria.where("name").is(encryptedFieldNameValue));
    System.out.println("query = " + query2.toString());

    Document c2 = mongoTemplate.findOne(query2, Document.class, Config.collName);
    System.out.println("Encrypt Document : " + c2.toJson());

    System.out.println("Decrypt encryptedField : " + clientEncryption
        .decrypt(new BsonBinary(c2.get("name", Binary.class).getData())));
  }
}