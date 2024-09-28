package com.spring.mongo.controller;

import org.springframework.http.server.RequestPath;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.mongo.model.Person;
import com.spring.mongo.repo.PersonAuditTrail;
import com.spring.mongo.repo.ReactivePersonAuditTrailRepository;
import com.spring.mongo.repo.ReactivePersonRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class ReactivePersonController {
    private final ReactivePersonRepository reactivePersonRepository;
    private final ReactivePersonAuditTrailRepository reactivePersonAuditTrailRepository;


    @PostMapping
    public Mono<Person> createUpdate(@RequestBody Mono<Person> personMono){
        return personMono.flatMap(reactivePersonRepository::save);
    }

    @GetMapping
    public Flux<Person> getAll(){
        return reactivePersonRepository.findAll();
    }

    @GetMapping("/audit")
    public Flux<PersonAuditTrail> getAllAudit(){
        return reactivePersonAuditTrailRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@RequestParam final String id){
        return reactivePersonRepository.deleteById(id);
    }
}
