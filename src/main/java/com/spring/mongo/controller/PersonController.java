package com.spring.mongo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.mongo.model.Person;
import com.spring.mongo.repo.PersonAuditTrail;
import com.spring.mongo.repo.PersonAuditTrailRepository;
import com.spring.mongo.repo.PersonRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonRepository personRepository;
    private final PersonAuditTrailRepository auditTrailRepository;


    @PostMapping
    public Person createUpdate(@RequestBody Person person){
        return personRepository.save(person);
    }

    @GetMapping
    public Iterable<Person> getAll(){
        return personRepository.findAll();
    }

    @GetMapping("/audit")
    public Iterable<PersonAuditTrail> getAllAudit(){
        return auditTrailRepository.findAll();
    }

}
