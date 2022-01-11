package com.example.demofle.templ;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClients;

public class BasicConnectAndWrite {

  public static void main(String[] args) throws Exception {

    String connectionString = "mongodb://c:c@13.214.135.136:27077";
    MongoOperations mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "test");

    Customer customer = new Customer(); 
    customer.setFirstName("Caspar"); 
    customer.setLastName("Chang"); 
    mongoTemplate.save(customer);

    Document c = new Document()
      .append("firstName", "Esther")
      .append("lastName", "Yu");
    
    mongoTemplate.save(c, "customer");
  }
}