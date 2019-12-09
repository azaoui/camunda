package io.poc.process.model;

public class Person {
    private String name;

    private String email;

    public Person() {
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Person(String name, String email) {

        this.name = name;
        this.email = email;
    }



    public String getName() {
        return name;
    }
}
