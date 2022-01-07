package com.example.demofle.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import org.bson.BsonDocument;
import org.bson.Document;

public class WriteEncrypt {
  
    public static void main(String[] args) throws IOException{

        String connectionString = "mongodb://c:c@13.214.135.136:27077";
        String keyVaultNamespace = "encryption.__keyVault";
        
        String path = "master-key.txt";
        byte[] localMasterKey= new byte[96];
        try (FileInputStream fis = new FileInputStream(path)) {
            fis.readNBytes(localMasterKey, 0, 96);
        }
        
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put("key", localMasterKey);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>();
        kmsProviders.put("local", keyMap);

        //HashMap<String, BsonDocument> schemaMap = new HashMap<String, BsonDocument>();
        //schemaMap.put("medicalRecords.patients", BsonDocument.parse(jsonSchema));

        Map<String, Object> extraOptions = new HashMap<String, Object>();
        extraOptions.put("mongocryptdSpawnPath", "/usr/local/bin/mongocryptd");

        MongoClientSettings clientSettings = MongoClientSettings.builder()
    .applyConnectionString(new ConnectionString(connectionString))
    .autoEncryptionSettings(AutoEncryptionSettings.builder()
        .keyVaultNamespace(keyVaultNamespace)
        .kmsProviders(kmsProviders)
        //.schemaMap(schemaMap)
        .extraOptions(extraOptions)
        .build())
    .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);
        
        Document patient = new Document()
        .append("name", "name")
        .append("ssn", "ssn")
        .append("bloodType", "bloodType")
        .append("medicalRecords", "medicalRecords");

        MongoDatabase database = mongoClient.getDatabase("hos");
        MongoCollection<Document> collection = database.getCollection("pat");
        InsertOneResult result = collection.insertOne(patient);

    }
}
