package com.example.demofle.mongoClient;

import org.bson.Document;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import com.example.demofle.config.Config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

public class BasicConnectAndWrite {

    public static void main(String[] args) throws Exception {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        MongoClient mongoClient = MongoClients.create(Config.connectionString);
        MongoDatabase database = mongoClient.getDatabase(Config.dbName);
        MongoCollection<Document> collection = database.getCollection(Config.collName);

        Document doc = new Document()
                .append("firstName", "Caspar")
                .append("lastName", "Chang")
                .append("age", 36);

        InsertOneResult result = collection.insertOne(doc);

        if (result != null)
            System.out.println("query1 : " + result.toString());

    }

}