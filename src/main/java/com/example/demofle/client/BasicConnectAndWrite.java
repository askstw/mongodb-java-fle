package com.example.demofle.client;

import org.bson.Document;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

public class BasicConnectAndWrite {

    public static void main( String[] args ) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        String uri = "mongodb://c:c@13.214.135.136:27077";
        MongoClient mongoClient = MongoClients.create(uri);
        
        MongoDatabase database = mongoClient.getDatabase("fle");
        MongoCollection<Document> collection = database.getCollection("customer");

        Document doc = new Document()
            .append("name", "caspar")
            .append("ssn", "123456");

        InsertOneResult result = collection.insertOne(doc);

        if (result != null)
            System.out.println(result.toString());
        
    }

}