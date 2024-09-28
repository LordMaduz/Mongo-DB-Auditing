package com.spring.mongo.listener;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bson.BsonDocument;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveChangeStreamOperation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.FullDocumentBeforeChange;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReactiveChangeStreamListener implements ApplicationListener<ApplicationReadyEvent> {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private static final String AUDIT_COLLECTION_SUFFIX = "_audit_trail";
    private static final String COLLECTION_FIELD = "coll";

    @PostConstruct
    public void onStart() {
        reactiveMongoTemplate.getMongoDatabase()
            .subscribe(database -> {

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("collMod", "person_collection");
                map.put("changeStreamPreAndPostImages", Map.of("enabled", true));

                Publisher<Document> commandResult = database.runCommand(new Document(map));
                commandResult.subscribe(new BaseSubscriber<>() {
                });
            });

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        listenToChangeStream();
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

    public void listenToChangeStream() {
        Consumer<ChangeStreamOptions.ChangeStreamOptionsBuilder> options = changeStreamOptionsBuilder ->
            changeStreamOptionsBuilder
                .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
                .fullDocumentBeforeChangeLookup(FullDocumentBeforeChange.REQUIRED)
                .filter(newAggregation(Aggregation.match(Criteria.where("ns.coll")
                    .not()
                    .regex("_audit_trail$", "m"))))
                .build();

        ReactiveChangeStreamOperation.TerminatingChangeStream<?> changeStreamOperation;

        BsonDocument resumeToken = readResumeTokenFromFile();
        if (resumeToken == null) {
            changeStreamOperation = reactiveMongoTemplate.changeStream(Document.class)
                .withOptions(options);
        } else {
            changeStreamOperation = reactiveMongoTemplate.changeStream(Document.class)
                .withOptions(options)
                .resumeAfter(resumeToken);
        }
        Disposable subscription = changeStreamOperation.listen()
            .doOnNext(changeStreamEvent -> {
                BsonDocument document = (BsonDocument) changeStreamEvent.getResumeToken();
                assert document != null;
                saveResumeTokenToFile(document);
                log.info("{}", changeStreamEvent);

                ChangeStreamDocument<Document> changeStreamDocument = changeStreamEvent.getRaw();

                assert changeStreamDocument != null;
                Document auditTrailDocument = getAuditTrailDocument(changeStreamDocument);
                BsonDocument nameSpace = changeStreamDocument.getNamespaceDocument();
                assert nameSpace != null;

                final String auditCollectionName = getAuditCollection(nameSpace.get(COLLECTION_FIELD)
                    .asString()
                    .getValue());

                reactiveMongoTemplate.insert(auditTrailDocument, auditCollectionName)
                    .subscribe();
            })
            .subscribe();

        Runtime.getRuntime()
            .addShutdownHook(new Thread(() -> {
                log.info("Shutting down application ...");
                subscription.dispose();
            }));
    }

    private Document getAuditTrailDocument(ChangeStreamDocument<Document> changeStreamDocument) {
        Map<String, Object> auditMap = new LinkedHashMap<>();
        auditMap.put("updatedDocument", changeStreamDocument.getFullDocument());
        auditMap.put("documentBeforeUpdate", changeStreamDocument.getFullDocumentBeforeChange());
        auditMap.put("createdDateTime", LocalDateTime.now());

        return new Document(auditMap);
    }

    private void saveResumeTokenToFile(BsonDocument resumeToken) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resume_token.txt"))) {
            writer.write(resumeToken.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BsonDocument readResumeTokenFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("resume_token.txt"))) {
            String tokenJson = reader.readLine();
            return BsonDocument.parse(tokenJson);
        } catch (IOException e) {
            log.info("FILE Log Present");
        }
        return null;
    }

    private String getAuditCollection(final String collection) {
        return collection.concat(AUDIT_COLLECTION_SUFFIX);
    }

}
