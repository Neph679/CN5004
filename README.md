# Crypto/Stock Portfolio Tracker

A JavaFX desktop application for managing a digital portfolio of stocks and cryptocurrencies. The user can record `BUY` and `SELL` transactions, view transaction history, monitor current holdings, and calculate profit or loss using an object-oriented design.

## Features

- Record stock and crypto transactions
- Track full transaction history
- Persist transactions to a CSV file with File I/O
- View current holdings in a JavaFX table
- Calculate:
  - total invested capital
  - current portfolio value
  - realized profit/loss
  - unrealized profit/loss
  - total profit/loss
- Clean JavaFX interface styled with external CSS

## Technologies

- Java 17
- JavaFX 21
- Maven
- FXML
- CSS

## Project Structure

```text
src/
  main/
    java/
      org/example/cryptostockportfoliotracker/
        Launcher.java
        MainApp.java
        controller/
          PortfolioController.java
        model/
          Asset.java
          AssetCategory.java
          Investor.java
          PortfolioHolding.java
          PortfolioSummary.java
          Transaction.java
          TransactionType.java
        service/
          PortfolioFileRepository.java
          PortfolioService.java
    resources/
      org/example/cryptostockportfoliotracker/
        portfolio-view.fxml
        portfolio-theme.css
data/
  transactions.csv
```

## OOP Design

The application follows a layered OOP structure:

- `model`: business entities and portfolio data
- `service`: business logic and file persistence
- `controller`: JavaFX UI logic
- `resources`: FXML layout and CSS styling

Main classes:

- `Investor`: stores the investor and transaction list
- `Asset`: represents a stock or cryptocurrency
- `Transaction`: represents one buy/sell operation
- `PortfolioHolding`: represents the current state of one asset in the portfolio
- `PortfolioSummary`: aggregates total portfolio metrics
- `PortfolioService`: validates transactions and calculates portfolio values
- `PortfolioFileRepository`: saves and loads transactions from CSV

## How It Works

1. The application starts through `Launcher` or `MainApp`.
2. `MainApp` loads the JavaFX layout from `portfolio-view.fxml`.
3. `PortfolioController` initializes the UI and loads saved transactions from `data/transactions.csv`.
4. When the user adds a transaction:
   - input is read from the form
   - an `Asset` object is created
   - `PortfolioService` validates and records the transaction
   - the transaction is appended to the CSV file
   - the tables and summary labels are refreshed

## File Persistence

Transaction history is stored in:

```text
data/transactions.csv
```

CSV header:

```text
timestamp,investor,symbol,name,category,type,quantity,unitPrice
```

This ensures the project satisfies the File I/O requirement of the assignment.

## Build and Run

Compile:

```bash
./mvnw compile
```

Run:

```bash
./mvnw javafx:run
```

## Academic Requirements Covered

This project addresses the main assignment requirements:

- Desktop application with JavaFX
- OOP architecture with multiple classes
- Buy and sell transaction handling
- File I/O transaction history
- Portfolio valuation and profit/loss calculation
- Visualization through JavaFX tables and summary fields

## Notes

- The current portfolio value is based on the latest recorded transaction price for each asset.
- The default investor name is fixed in the current implementation.
- The CSV file is automatically created if it does not already exist.

## Documentation

For a detailed student-oriented explanation of the architecture, logic, and code, see:

`DETAILED_PROJECT_EXPLANATION.md`
