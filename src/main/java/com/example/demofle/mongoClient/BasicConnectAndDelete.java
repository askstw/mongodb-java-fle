package com.example.demofle.mongoClient;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

public class BasicConnectAndDelete {

    public static void main( String[] args ) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        String connectionString = "mongodb://c:c@13.214.135.136:27077";
        String dbName = "test";
        String collName = "customer";

        MongoClient mongoClient = MongoClients.create(connectionString);
        
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(collName);

        //Bson query = Filters.eq("firstName", "Caspar");  
        Bson query = new BsonDocument();
        
        DeleteResult result = collection.deleteMany(query);

        if (result != null)
            System.out.println(result.toString());
        
    }

}