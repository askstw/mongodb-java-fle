package com.example.demofle.mongoTemplate;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClients;

public class BasicConnectAndWrite {

  public static void main(String[] args) throws Exception {

    String connectionString = "mongodb://c:c@13.214.135.136:27077";
    String dbName = "test";
    String collName = "customer";

    MongoOperations mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), dbName);

    Customer customer = new Customer();
    customer.setFirstName("Caspar");
    customer.setLastName("Chang");
    customer.setAge(36);
    mongoTemplate.save(customer);

    Document c = new Document()
        .append("firstName", "Esther")
        .append("lastName", "Yu")
        .append("age", 32);
    mongoTemplate.save(c, collName);
  }
}