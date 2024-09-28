package com.spring.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Document("person_collection")
@Data
public class Person extends Entity{

    @Id
    private String id;
    private String name;
    private School school;




    @Data
    public static class School{
        private String name;
        private List<Class> classList;
    }

    @Data
    public static class Class{
        private String name;
    }

    public void print(){
        System.out.println();
    }
}
