package com.spring.mongo.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.spring.mongo.model.Person;

@Repository
public interface PersonRepository
    extends CrudRepository<Person, String>
{

}
