package com.example.demofle.mongoTemplate;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.example.demofle.config.Config;
import com.mongodb.client.MongoClients;

public class BasicConnectAndDelete {

  public static void main(String[] args) throws Exception {

    MongoOperations mongoTemplate = new MongoTemplate(MongoClients.create(Config.connectionString), Config.dbName);

    Query query = new Query();
    mongoTemplate.remove(query, Config.collName);
  }
}