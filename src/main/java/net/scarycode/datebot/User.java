package net.scarycode.datebot;

import javax.persistence.*;

enum Gender{male,female}

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "dater")
public class User {
    @Id
    private String id;

    private String name;

    private Gender gender;

    public User() {
    }

    public User(String id, String name, Gender gender) {
        this.id = id;
        this.name = name;
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}