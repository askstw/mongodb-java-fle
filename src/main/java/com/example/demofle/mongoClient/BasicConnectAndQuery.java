package com.example.demofle.mongoClient;

import static com.mongodb.client.model.Filters.eq;
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

public class BasicConnectAndQuery {

    public static void main(String[] args) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        String connectionString = "mongodb://c:c@13.214.135.136:27077";
        String dbName = "test";
        String collName = "customer";

        MongoClient mongoClient = MongoClients.create(connectionString);

        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(collName);

        Document doc1 = collection.find(eq("firstName", "Caspar")).first();
        if (doc1 != null)
            System.out.println("query1 : " + doc1.toJson());

        Bson query2 = Filters.gt("age", 30);
        System.out.println("query2 : " + collection.find(query2).first().toJson());

    }

}