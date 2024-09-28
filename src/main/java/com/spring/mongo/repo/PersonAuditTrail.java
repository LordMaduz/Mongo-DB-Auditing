package com.spring.mongo.repo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("person_collection_audit_trail")
public class PersonAuditTrail extends AuditTrail{

    @Id
    private String id;
}
