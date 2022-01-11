package com.example.demofle.templ;

import org.springframework.data.annotation.Id;


public class Customer {

  @Id
  public String id;

  public String firstName;
  public String lastName;
  public String age;

  public Customer() {}

  public Customer(String firstName, String lastName, String age) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public void setFirstName(String firstName){
    this.firstName = firstName;
  }

  public void setLastName(String lastName){
    this.lastName = lastName;
  }

  public void setAge(String age){
    this.age = age;
  }

  @Override
  public String toString() {
    return String.format(
        "Customer[id=%s, firstName='%s', lastName='%s', age='%s']",
        id, firstName, lastName, age);
  }

}