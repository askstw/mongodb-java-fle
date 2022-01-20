package com.example.demofle.mongoClient;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import com.example.demofle.config.Config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class BasicConnectAndDelete {

    public static void main( String[] args ) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        MongoClient mongoClient = MongoClients.create(Config.connectionString);
        MongoDatabase database = mongoClient.getDatabase(Config.dbName);
        MongoCollection<Document> collection = database.getCollection(Config.collName);

        //Bson query = Filters.eq("firstName", "Caspar");  
        //Delete All Data
        Bson query = new BsonDocument();
        DeleteResult result = collection.deleteMany(query);

        if (result != null)
            System.out.println(result.toString());
        
    }

}