package com.example.demofle.mongoClient;

import java.io.IOException;
import java.util.Base64;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class CheckDataKey {
    public static void main(String[] args) throws IOException {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        
        String connectionString = "mongodb://c:c@13.214.135.136:27077";
        String keyVaultDb = "encryption";
        String keyVaultCollection = "__keyVault";
        String base64KeyId = "87j9MH/FR6e9x2PIXkBiaQ==";
        
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoCollection<Document> collection = mongoClient.getDatabase(keyVaultDb).getCollection(keyVaultCollection);
        
        Bson query = Filters.eq("_id", new Binary((byte) 4, Base64.getDecoder().decode(base64KeyId)));
        Document doc = collection
            .find(query)
            .first();

        Binary b = (Binary) doc.get("_id");
        System.out.println("encodeDataKey = " + Base64.getEncoder().encodeToString(b.getData()));        

        
    }
}