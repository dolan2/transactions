package org.finance.transactions.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final String MIGRATIONS_COLLECTION = "schema_migrations";

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running database migrations");
        runIfNotApplied("V1__create_transaction_indexes", this::v1CreateTransactionIndexes);
        runIfNotApplied("V2__create_import_job_indexes", this::v2CreateImportJobIndexes);
        log.info("Database migrations completed");
    }

    private void runIfNotApplied(String migrationId, Runnable migration) {
        boolean alreadyApplied = mongoTemplate.exists(
                Query.query(Criteria.where("_id").is(migrationId)), MIGRATIONS_COLLECTION);

        if (alreadyApplied) {
            log.debug("Migration {} already applied, skipping", migrationId);
            return;
        }

        migration.run();
        mongoTemplate.save(new MigrationRecord(migrationId, LocalDateTime.now()), MIGRATIONS_COLLECTION);
        log.info("Migration {} applied successfully", migrationId);
    }

    private void v1CreateTransactionIndexes() {
        MongoCollection<Document> col = mongoTemplate.getDb().getCollection("transactions");
        createIndex(col, new Document("iban", 1), "idx_transaction_iban");
        createIndex(col, new Document("date", 1), "idx_transaction_date");
        createIndex(col, new Document("category", 1), "idx_transaction_category");
        createIndex(col, new Document("iban", 1).append("date", 1), "idx_transaction_iban_date");
        createIndex(col, new Document("category", 1).append("date", 1), "idx_transaction_category_date");
    }

    private void v2CreateImportJobIndexes() {
        MongoCollection<Document> col = mongoTemplate.getDb().getCollection("import_jobs");
        createIndex(col, new Document("status", 1), "idx_import_job_status");
        createIndex(col, new Document("createdAt", -1), "idx_import_job_created_at");
    }

    private void createIndex(MongoCollection<Document> col, Document key, String name) {
        col.createIndex(key, new IndexOptions().name(name));
    }

    private record MigrationRecord(String id, LocalDateTime appliedAt) {}
}
