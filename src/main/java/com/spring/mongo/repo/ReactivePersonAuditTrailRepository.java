package com.spring.mongo.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactivePersonAuditTrailRepository extends ReactiveCrudRepository<PersonAuditTrail, String> {

}

