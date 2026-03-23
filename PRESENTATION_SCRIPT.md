# Presentation Script

## 1. Introduction

Hello, my project is a desktop application called **Crypto/Stock Portfolio Tracker**.

The purpose of the application is to help a user manage a digital portfolio containing either cryptocurrencies or stocks. The user can record buy and sell transactions, store them permanently in a file, see the current portfolio status, and calculate profit or loss.

This project was developed using **Java**, **JavaFX**, **FXML**, **CSS**, and **Maven**, and it follows an **object-oriented design**.

## 2. Main Requirements Covered

The assignment required:

- a desktop application
- buying and selling digital assets
- proper OOP architecture
- transaction history stored in a file
- calculation of profit and loss
- visualization of the portfolio with JavaFX

My project covers all of these requirements.

## 3. General Idea Of The System

The system works by storing every user action as a transaction.

Each transaction contains:

- the investor name
- the asset symbol
- the asset name
- the asset category
- whether it is a buy or sell
- the quantity
- the unit price
- the timestamp

From these transactions, the system reconstructs the current holdings and calculates the portfolio summary.

## 4. Architecture

The project is divided into three main parts:

### Model layer

This contains the business entities:

- `Asset`
- `Transaction`
- `Investor`
- `PortfolioHolding`
- `PortfolioSummary`
- enums for category and transaction type

### Service layer

This contains the logic:

- `PortfolioService` for validation and calculations
- `PortfolioFileRepository` for File I/O

### UI layer

This contains:

- `PortfolioController`
- `portfolio-view.fxml`
- `portfolio-theme.css`

This separation is important because it keeps the code clean and maintainable.

## 5. Important Classes

### `Asset`

Represents one stock or cryptocurrency.

It stores:

- symbol
- name
- category

### `Transaction`

Represents a buy or sell operation.

This is the historical record of the portfolio.

### `Investor`

Stores the investor name and all transactions.

### `PortfolioHolding`

Represents the current state of an asset after processing many transactions.

This class is very important because it calculates:

- quantity owned
- average cost
- current value
- realized profit
- unrealized profit

### `PortfolioSummary`

Represents the complete summary of the portfolio:

- holdings list
- total invested
- portfolio value
- realized profit
- unrealized profit

## 6. File I/O

To satisfy the assignment requirement for file handling, I used a CSV file:

`data/transactions.csv`

Every time the user records a new transaction, the application appends it to the file.

When the application starts, it reads the file again and rebuilds the transaction history.

This means the data is persistent even after the application closes.

## 7. Business Logic

The most important logic is inside `PortfolioService`.

When a transaction is recorded:

1. the input is validated
2. a transaction object is created
3. if it is a sell, the system checks if enough quantity exists
4. the transaction is stored in memory
5. the transaction is appended to the CSV file

Then the system rebuilds the holdings and updates the summary values.

## 8. Profit And Loss Calculation

The application calculates:

### Total invested

The sum of all buy transactions.

### Realized profit

Profit from assets that have already been sold.

Formula:

`sold quantity × (sell price - average cost)`

### Unrealized profit

Profit from assets still held in the portfolio.

Formula:

`market value - cost basis`

### Total profit/loss

The sum of realized and unrealized profit.

## 9. JavaFX User Interface

The interface was built with JavaFX and FXML.

It contains:

- a transaction input form
- a transaction history table
- a holdings table
- a portfolio summary section

The style is separated into an external CSS file so that the structure and design are not mixed together.

## 10. Why The Design Is Object-Oriented

I used OOP because the problem naturally contains objects:

- investor
- asset
- transaction
- holding
- summary

Each class has a clear responsibility, which makes the application easier to understand, easier to extend, and easier to maintain.

## 11. Strengths Of The Project

- clear class structure
- proper use of OOP
- working File I/O
- transaction validation
- profit/loss calculations
- JavaFX visualization
- external styling with CSS

## 12. Limitations And Future Improvements

Some possible improvements are:

- support for multiple investors
- connection to live market price APIs
- charts and graphs
- edit or delete transaction functionality
- unit testing

## 13. Closing

In conclusion, this project is a complete desktop portfolio tracker that meets the assignment requirements and demonstrates object-oriented programming, file handling, JavaFX user interface development, and financial calculation logic.

If needed, I can also explain each file in more detail and walk through the code step by step.
