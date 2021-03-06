package com.example.demofle.mongoTemplate;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.demofle.config.Config;
import com.mongodb.client.MongoClients;

public class BasicConnectAndWrite {

  public static void main(String[] args) throws Exception {

    MongoOperations mongoTemplate = new MongoTemplate(MongoClients.create(Config.connectionString), Config.dbName);

    Customer customer = new Customer();
    customer.setFirstName("Caspar");
    customer.setLastName("Chang");
    customer.setAge(36);
    mongoTemplate.save(customer);

    Document c = new Document()
        .append("firstName", "Esther")
        .append("lastName", "Yu")
        .append("age", 32);
    mongoTemplate.save(c, Config.collName);
  }
}