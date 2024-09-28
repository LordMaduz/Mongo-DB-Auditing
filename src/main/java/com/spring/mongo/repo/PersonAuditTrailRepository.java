package com.spring.mongo.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAuditTrailRepository
    extends CrudRepository<PersonAuditTrail, String>
{

}
