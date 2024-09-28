package com.spring.mongo.model;

import lombok.Data;

@Data
public class Entity {

    private String field;

    public void accessField(){
        print(this.field);
    }

    public String print(final String field){
        return field;
    }


}
