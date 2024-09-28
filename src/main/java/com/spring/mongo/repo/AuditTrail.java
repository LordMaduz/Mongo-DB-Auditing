package com.spring.mongo.repo;

import java.time.LocalDateTime;

import org.bson.Document;
import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Data
public class AuditTrail {

    private Document updatedDocument;
    private Document documentBeforeUpdate;
    private Document change;
    @CreatedDate
    private LocalDateTime createdDateTime;
}
