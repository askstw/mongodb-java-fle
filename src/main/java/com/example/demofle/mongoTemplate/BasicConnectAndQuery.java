package com.example.demofle.mongoTemplate;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.client.MongoClients;

public class BasicConnectAndQuery {

  public static void main(String[] args) throws Exception {

    String connectionString = "mongodb://c:c@13.214.135.136:27077";
    String dbName = "test";
    String collName = "customer";

    MongoOperations mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), dbName);

    Query query = Query.query(Criteria.where("firstName").is("Caspar"));
    System.out.println("query = " + query.toString());

    Customer customer = mongoTemplate.findOne(query, Customer.class);
    System.out.println("customer = " + customer.toString());

    Document c = mongoTemplate.findOne(query, Document.class, collName);
    System.out.println("document = " + c.toString());
  }
}