package com.example.demofle.mongoTemplate;

import org.springframework.data.annotation.Id;


public class Customer {

  @Id
  public String id;

  public String firstName;
  public String lastName;
  public int age;

  public Customer() {}

  public Customer(String firstName, String lastName, int age) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }

  public void setFirstName(String firstName){
    this.firstName = firstName;
  }

  public void setLastName(String lastName){
    this.lastName = lastName;
  }

  public void setAge(int age){
    this.age = age;
  }

  public int getAge(){
    return age;
  }

  @Override
  public String toString() {
    return String.format(
        "Customer[id=%s, firstName='%s', lastName='%s', age='%s']",
        id, firstName, lastName, age);
  }

}