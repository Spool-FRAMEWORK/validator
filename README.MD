# Spool — Validator

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36.svg)](https://maven.apache.org/)

**Validator** is the **Spool** framework module that provides **pluggable business-rule validation for event payloads**. Validators are auto-discovered via annotation and executed by the Ingester before persisting each envelope.

---

## Table of Contents

- [What it does](#what-it-does)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Examples](#examples)
  - [Basic Validator](#basic-validator)
  - [Multiple Validators for the Same Source](#multiple-validators-for-the-same-source)
  - [Running Validators Manually](#running-validators-manually)
- [Key Concepts](#key-concepts)
- [Validation Outcomes](#validation-outcomes)
- [License](#license)

---

## What it does

```
Ingester receives envelope
      │
      ▼
ValidatorRegistry.validateAll(sourceId, event)
      │
      ├── runs all @Validate validators for that sourceId + event type
      │
      ▼
ValidationResult
      │
      ├── VALID       → persist normally
      ├── WARN        → persist with warning log
      └── QUARANTINE  → send to QuarantineStore, skip persistence
```

Validators are declared with `@Validate` and auto-loaded via `ServiceLoader`. No manual registration needed.

---

## Installation

```xml
<dependency>
    <groupId>io.github.spool-framework</groupId>
    <artifactId>validator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Spool-FRAMEWORK/*</url>
    </repository>
</repositories>
```

> Requires **Java 21+**.

---

## Quick Start

### 1. Implement and annotate a validator

```java
@Validate(value = OrderEvent.class, sourceId = "orders-source")
public class CurrencyValidator implements Validator<OrderEvent> {

    @Override
    public boolean validate(OrderEvent event) {
        return List.of("EUR", "USD", "GBP").contains(event.currency());
    }

    @Override
    public String failMessage(OrderEvent event) {
        return "Currency '" + event.currency() + "' is not accepted";
    }

    @Override
    public String failedField() {
        return "currency";
    }
}
```

### 2. Register via ServiceLoader

Add the class to `META-INF/services/software.spool.validator.api.Validator`:

```
com.example.CurrencyValidator
```

The Ingester's `ValidatorRegistry` will discover and run it automatically for any envelope with `sourceId = "orders-source"`.

---

## Examples

### Basic Validator

```java
@Validate(value = TradeEvent.class, sourceId = "gemini-btcusd", severity = Severity.CRITICAL)
public class TradeAmountValidator implements Validator<TradeEvent> {

    @Override
    public boolean validate(TradeEvent event) {
        return event.amount() != null && event.amount().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String failMessage(TradeEvent event) {
        return "Trade amount must be positive, got: " + event.amount();
    }

    @Override
    public String failedField() {
        return "amount";
    }
}
```

---

### Multiple Validators for the Same Source

Multiple validators can target the same `sourceId` and event type. They run in `order` (lower = first).

```java
@Validate(value = OrderEvent.class, sourceId = "orders-source", order = 1)
public class OrderIdValidator implements Validator<OrderEvent> { ... }

@Validate(value = OrderEvent.class, sourceId = "orders-source", order = 2)
public class CurrencyValidator implements Validator<OrderEvent> { ... }

@Validate(value = OrderEvent.class, sourceId = "orders-source", severity = Severity.WARN)
public class OptionalFieldsValidator implements Validator<OrderEvent> { ... }
```

---

### Running Validators Manually

You can also run validators directly without the Ingester pipeline.

```java
ValidatorRegistry registry = new ValidatorRegistry();

// Fail-fast: throws ValidationException on the first failure
registry.validate("orders-source", event);

// Collect all results
ValidationResult result = registry.validateAll("orders-source", event);

if (result.isQuarantine()) {
    result.getViolations().forEach(v -> log.error("CRITICAL: {}", v.getMessage()));
} else if (result.hasWarnings()) {
    result.getViolations().forEach(v -> log.warn("WARN: {}", v.getMessage()));
}
```

---

## Key Concepts

| Concept | Description |
|---|---|
| **`@Validate`** | Annotation that links a `Validator<T>` to an event type and a `sourceId`. |
| **`Validator<T>`** | Interface with `validate(T)` → `boolean`, `failMessage(T)`, `failedField()`. |
| **`ValidationResult`** | Aggregates all violations and computes the overall outcome. |
| **`ValidatorRegistry`** | Loads validators via `ServiceLoader` and runs them by sourceId + event type. |
| **`severity`** | `CRITICAL` → quarantine on failure. Non-critical → warn but allow persistence. |
| **`order`** | Execution order within the same sourceId + event type group. Lower runs first. |

---

## Validation Outcomes

| Outcome | Condition | Ingester action |
|---|---|---|
| `VALID` | No violations. | Persist normally. |
| `WARN` | One or more non-critical violations. | Persist with warning. |
| `QUARANTINE` | At least one `CRITICAL` violation. | Send to `QuarantineStore`, skip persistence. |

---

## License

Distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
