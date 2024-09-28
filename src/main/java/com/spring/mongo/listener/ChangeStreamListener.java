package com.spring.mongo.listener;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.Subscription;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.FullDocumentBeforeChange;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChangeStreamListener implements ApplicationListener<ApplicationReadyEvent> {

    private final MongoTemplate mongoTemplate;
    private static final String AUDIT_COLLECTION_SUFFIX = "_audit_trail";
    private static final String NAMESPACE_COLLECTION = "coll";

    @PostConstruct
    public void onStart() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("collMod", "person_collection");
        map.put("changeStreamPreAndPostImages", Map.of("enabled", true));
        mongoTemplate.getDb()
            .runCommand(new Document(map));
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        ChangeStreamOptions options = ChangeStreamOptions.builder()
            .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
            .filter(newAggregation(Aggregation.match(Criteria.where("ns.coll")
                .not()
                .regex("_audit_trail$", "m"))))
            .fullDocumentBeforeChangeLookup(FullDocumentBeforeChange.REQUIRED)
            .build();

        MessageListener<ChangeStreamDocument<Document>, Document> messageListener = (changeStreamEvent) -> {
            ChangeStreamDocument<Document> changeStreamDocument = changeStreamEvent.getRaw();
            assert changeStreamDocument != null;
            BsonDocument nameSpace = changeStreamDocument.getNamespaceDocument();
            assert nameSpace != null;

            Document auditTrailDocument = getAuditTrailDocument(changeStreamDocument);

            final String auditCollectionName = getAuditCollection(nameSpace.get(NAMESPACE_COLLECTION)
                .asString()
                .getValue());
            mongoTemplate.insert(auditTrailDocument, auditCollectionName);
        };

        ChangeStreamRequest<Document> changeStreamRequest = new ChangeStreamRequest<>(messageListener,
            new ChangeStreamRequest.ChangeStreamRequestOptions(null, null, options));

        Subscription subscription = getMessageListenerContainer().register(changeStreamRequest, Document.class);

    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

    private String getAuditCollection(final String collection) {
        return collection.concat(AUDIT_COLLECTION_SUFFIX);
    }

    private MessageListenerContainer getMessageListenerContainer() {
        MessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer(mongoTemplate);
        messageListenerContainer.start();
        return messageListenerContainer;
    }

    private Document getAuditTrailDocument(ChangeStreamDocument<Document> changeStreamDocument) {
        Map<String, Object> auditMap = new LinkedHashMap<>();
        auditMap.put("updatedDocument", changeStreamDocument.getFullDocument());
        auditMap.put("documentBeforeUpdate", changeStreamDocument.getFullDocumentBeforeChange());
        auditMap.put("createdDateTime", LocalDateTime.now());

        return new Document(auditMap);
    }
}
