# TP ICAP Matching Engine

A Java implementation of a trading matching engine with support for Price-Time-Priority and Pro-Rata matching strategies.

## Project Structure

This project follows the standard Maven directory layout:

```
src/
├── main/java/tpicap/
│   ├── domain/          # Core domain objects (Order, OrderResult, etc.)
│   ├── main/            # Application entry point
│   └── strategy/        # Matching strategy implementations
└── test/java/tpicap/
    └── test/            # JUnit test classes
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

To compile the project:

```bash
mvn clean compile
```

## Running Tests

To run all tests:

```bash
mvn test
```

The project includes comprehensive JUnit 5 tests for both matching strategies.

## Running the Application

To run the main application which demonstrates both matching strategies:

```bash
mvn exec:java -Dexec.mainClass="tpicap.main.MatchingEngineApp"
```

## User Stories Implemented

### User Story One: Price-Time-Priority Matching
- Orders are matched based on price priority (higher price buys first)
- Within the same price level, orders are matched by time priority (earlier orders first)
- Demonstrates matching with sample orders from the requirements

### User Story Two: Pro-Rata Matching
- Orders at the same price level are matched proportionally based on their volume
- Buy orders receive sell volume in proportion to their requested volume
- Demonstrates pro-rata distribution with sample orders

## Key Classes

- `Order`: Represents a trading order with ID, direction, volume, price, and timestamp
- `MatchingStrategy`: Interface for matching algorithms
- `PriceTimeStrategy`: Implements price-time-priority matching
- `ProRataStrategy`: Implements pro-rata matching
- `MatchingEngineApp`: Main application demonstrating both strategies

## Test Coverage

The project includes JUnit tests covering:
- Basic matching scenarios for both strategies
- Edge cases (empty order books, only buys/sells, etc.)
- Pro-rata distribution calculations
- Price-time priority ordering

All tests pass successfully.</content>
<parameter name="filePath">/workspaces/tpicap/README.md