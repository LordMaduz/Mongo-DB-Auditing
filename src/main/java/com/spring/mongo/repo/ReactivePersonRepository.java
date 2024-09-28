package com.spring.mongo.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.spring.mongo.model.Person;

@Repository
public interface ReactivePersonRepository extends ReactiveCrudRepository<Person, String> {

}
