# Claude Instructions — Bank Transactions API (DDD + Hexagonal)

## 🎯 Project Goal

Design and implement a production-ready REST API for importing and aggregating bank transactions.

The system should:

* Import CSV files with transactions
* Process them asynchronously
* Store and aggregate data
* Expose statistics per category, IBAN, and month

---

## 🧱 Architecture Principles

### 1. Domain-Driven Design (DDD)

* Clearly separate:

    * Domain
    * Application
    * Infrastructure
* Focus on business logic in Domain layer
* Use:

    * Aggregates
    * Value Objects
    * Entities
    * Domain Services

### 2. Hexagonal Architecture (Ports & Adapters)

Structure:

* Domain (core)
* Application (use cases)
* Ports (interfaces)
* Adapters (REST, DB, messaging)

Rules:

* Domain must NOT depend on Spring
* Infrastructure depends on Domain, not vice versa

---

## 🗂️ Project Structure

```
src/main/java/com/example/bank

  domain/
    model/
    service/
    repository/

  application/
    usecase/
    dto/

  adapter/
    in/
      rest/
    out/
      persistence/
      messaging/

  config/
```

---

## 🧠 Core Domain Concepts

### Entities

* Transaction
* ImportJob

### Value Objects

* IBAN
* Money (amount + currency)
* TransactionDate
* Category

### Aggregates

* ImportJob (controls processing lifecycle)

---

## ⚙️ Use Cases

### 1. Upload Transactions File

* Accept CSV file
* Create ImportJob
* Send message to RabbitMQ

### 2. Process File (Async Worker)

* Read CSV
* Validate rows
* Save transactions
* Update ImportJob status

### 3. Get Import Status

* Return processing status

### 4. Get Statistics

* By category
* By IBAN
* By month

---

## 📨 Messaging (RabbitMQ)

Use RabbitMQ for async processing:

* Queue: transaction-import
* Producer: REST API
* Consumer: Worker service

Message should contain:

* ImportJob ID
* File location

---

## 🗄️ Database (MongoDB)

Collections:

* transactions
* import_jobs

Indexes:

* IBAN
* date
* category

---

## 📊 Performance Requirements

* Must handle 10,000 rows per file
* Use streaming CSV parsing
* Avoid loading entire file into memory
* Batch inserts to MongoDB

---

## ✅ Validation Rules

Each row must validate:

* IBAN format
* Date format
* Currency not null
* Amount is numeric

Invalid rows:

* Skip but log
* Count errors in ImportJob

---

## 🌐 API Design

### POST /imports

Upload CSV file

Response:

* importId

### GET /imports/{id}

Check status

### GET /statistics

Query params:

* category
* iban
* month

---

## 🐳 Docker Setup

Use docker-compose with:

* app
* mongodb
* rabbitmq

Ensure:

* health checks
* proper networking

---

## 🧪 Testing Strategy

* Unit tests for domain logic
* Integration tests for adapters
* Testcontainers for MongoDB & RabbitMQ

---

## 🧩 Coding Standards

* Prefer immutability
* Small classes
* Max 1 responsibility per class
* No anemic domain
* Use constructor injection

---

## 🚀 Development Workflow (Spec-Driven)

### Step 1: Domain Modeling Agent

* Define entities and value objects
* Define aggregates

### Step 2: Use Case Agent

* Define application services
* Define ports

### Step 3: Infrastructure Agent

* Implement Mongo repositories
* Implement RabbitMQ adapters

### Step 4: API Agent

* Implement REST controllers

### Step 5: Integration Agent

* Wire everything with Spring Boot

### Step 6: DevOps Agent

* Create Dockerfile
* Create docker-compose

---

## 📌 Definition of Done

* API works end-to-end
* File processed asynchronously
* Statistics available
* Docker setup runs everything
* README explains usage

---

## ❗ Important Constraints

* Do NOT mix domain with infrastructure
* Do NOT put logic in controllers
* Do NOT use entities as DTOs

---

## 📚 Bonus

* Add retry mechanism for failed jobs
* Add idempotency
* Add pagination for statistics

---

This document defines how Claude should design and implement the system. Follow strictly.
