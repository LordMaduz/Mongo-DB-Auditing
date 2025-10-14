
# MongoDB Change Streams Auditing

> Real-time audit trail system for MongoDB using Change Streams with support for both reactive and non-reactive architectures.

## Overview

A Spring Boot application that automatically captures and audits all MongoDB document changes in real-time using MongoDB Change Streams. Supports both traditional Spring Data MongoDB and reactive Spring Data MongoDB implementations with pre-and-post image tracking.

**Key Features:**
- Automatic audit trail generation for all collections
- Pre-and-post document snapshots on every change
- Resume token persistence for crash recovery
- Dual support: Reactive (WebFlux) and Non-Reactive (MVC)
- Excludes audit collections from being audited (prevents infinite loops)
- OpenAPI/Swagger documentation
- Change stream filtering and aggregation

---

## Architecture
<img width="1199" height="1895" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-10-14-064116" src="https://github.com/user-attachments/assets/e7bbea6d-c208-42d6-ab0d-6e6c7ad33466" />

## Getting Started
#### Prerequisites

```bash
- Java 21+
- Maven 3.8+
- MongoDB 4.0+ configured as replica set
```

#### MongoDB Replica Set Setup
Change Streams require MongoDB to run as a replica set:

```bash
# Start MongoDB with replica set
mongod --replSet rs0 --port 27017 --dbpath /data/db
```

#### Initialize replica set (first time only)
```
mongosh
> rs.initiate()
```

#### Installation
1. Clone and Build
```bash
git clone <repository-url>
cd mongo-change-streams
mvn clean install
```

2. Configure Application
application.yml:

```yaml
spring:
  application:
    name: Mongo-Auditing
  main:
    web-application-type: reactive  # Change to 'servlet' for non-reactive
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27017/audit-database
```

4. Choose Architecture Mode
For Reactive (WebFlux) - Default:

```yaml
spring:
  main:
    web-application-type: reactive
```
Keep dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
Use: ReactiveChangeStreamListener, ReactivePersonController

For Non-Reactive (MVC):
```yaml
spring:
  main:
    web-application-type: servlet
```
Keep dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Use: ChangeStreamListener, PersonController

6. Run Application
```bash
mvn spring-boot:run
```
## Change Stream Features
### 1. Pre-and-Post Images

Captures document state before and after changes:

```javascript
// Enable on collection
db.runCommand({
  collMod: "person_collection",
  changeStreamPreAndPostImages: { enabled: true }
})
```

### 2. Aggregation Filtering
Excludes audit collections from being audited:

```java
Aggregation.match(Criteria.where("ns.coll")
    .not()
    .regex("_audit_trail$", "m"))
```
### 3. Resume Token Persistence
Enables crash recovery by saving stream position:

```java
// Save token to file
private void saveResumeTokenToFile(BsonDocument resumeToken) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("resume_token.txt"))) {
        writer.write(resumeToken.toJson());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// Read token on restart
private BsonDocument readResumeTokenFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader("resume_token.txt"))) {
        String tokenJson = reader.readLine();
        return BsonDocument.parse(tokenJson);
    } catch (IOException e) {
        log.info("No resume token found, starting fresh");
    }
    return null;
}
```

### 4. Full Document Lookup
Retrieves complete document on update operations:
```java
ChangeStreamOptions.builder()
    .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
    .fullDocumentBeforeChangeLookup(FullDocumentBeforeChange.REQUIRED)
```
