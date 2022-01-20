package com.example.demofle.keyManagement;

import java.io.IOException;
import java.util.Base64;

import com.example.demofle.config.Config;
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
        
        
        MongoClient mongoClient = MongoClients.create(Config.connectionString);
        MongoCollection<Document> collection = mongoClient.getDatabase(Config.keyVaultDb).getCollection(Config.keyVaultCollection);
        
        Bson query = Filters.eq("_id", new Binary((byte) 4, Base64.getDecoder().decode(Config.base64DataKeyId)));
        Document doc = collection
            .find(query)
            .first();

        Binary b = (Binary) doc.get("_id");
        System.out.println("encodeDataKey = " + Base64.getEncoder().encodeToString(b.getData()));        

        
    }
}