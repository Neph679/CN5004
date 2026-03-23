# Detailed Project Explanation

## Purpose of This Document

This file explains the entire project in a student-friendly way so you can present it, defend design choices, and describe how the application works internally.

The goal is not only to say what each file does, but also:

- why each class exists
- how the files collaborate
- how the OOP structure was chosen
- how File I/O works
- how JavaFX is connected to the Java code
- how profit and loss are calculated

## 1. What The Project Is

The project is a desktop application for portfolio management. It allows the user to:

- record `BUY` transactions
- record `SELL` transactions
- store transactions permanently in a file
- load previous transactions when the app starts
- display the full transaction history
- display the current holdings
- calculate gains and losses

This makes it a suitable implementation of a "Crypto/Stock Portfolio Tracker" assignment.

## 2. General Architecture

The project follows a clear layered architecture:

### Model layer

This layer contains the business entities:

- `Asset`
- `AssetCategory`
- `Transaction`
- `TransactionType`
- `Investor`
- `PortfolioHolding`
- `PortfolioSummary`

These classes represent the data of the problem domain.

### Service layer

This layer contains logic:

- `PortfolioService`
- `PortfolioFileRepository`

This is where the important calculations and file operations happen.

### Presentation layer

This layer is the user interface:

- `PortfolioController`
- `portfolio-view.fxml`
- `portfolio-theme.css`

This is where JavaFX displays the information and reacts to user actions.

### Application entry layer

This layer starts the program:

- `Launcher`
- `MainApp`

## 3. Folder Structure

```text
src/main/java/org/example/cryptostockportfoliotracker/
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

src/main/resources/org/example/cryptostockportfoliotracker/
  portfolio-view.fxml
  portfolio-theme.css

data/
  transactions.csv
```

This structure is good because each package has one responsibility.

## 4. Full Execution Flow

When the application starts:

1. `Launcher` runs.
2. `Launcher` calls `Application.launch(MainApp.class, args)`.
3. JavaFX creates the application and calls `MainApp.start(Stage stage)`.
4. `MainApp` loads the file `portfolio-view.fxml`.
5. Because the FXML file declares a controller, JavaFX creates `PortfolioController`.
6. JavaFX injects all `@FXML` fields into the controller.
7. JavaFX calls `initialize()`.
8. `initialize()`:
   - fills the combo boxes
   - configures the table columns
   - loads transactions from `transactions.csv`
   - builds the current summary
9. The window appears.

When the user presses `Record Transaction`:

1. JavaFX triggers `addTransaction()`.
2. The controller reads the form values.
3. The controller creates an `Asset`.
4. The controller calls `portfolioService.recordTransaction(...)`.
5. The service validates the input.
6. The service creates a `Transaction`.
7. The service checks whether a sell is allowed.
8. The service adds the transaction to memory.
9. The service appends the transaction to the CSV file.
10. The controller reloads the observable data.
11. The controller refreshes the portfolio summary labels.

## 5. File-by-File Explanation

## 5.1 `Launcher.java`

```java
package org.example.cryptostockportfoliotracker;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
```

Explanation:

- The `package` line declares the namespace of the class.
- `import javafx.application.Application;` is needed because `launch` belongs to JavaFX's `Application` class.
- `public class Launcher` defines a very small bootstrap class.
- `public static void main(String[] args)` is the standard Java entry point.
- `Application.launch(MainApp.class, args);` tells JavaFX to start the real application class.

Why this file exists:

- In some JavaFX/Maven setups, having a simple launcher class avoids startup/module issues.
- It acts as a clean external entry point.

## 5.2 `MainApp.java`

```java
public class MainApp extends Application {
```

This means `MainApp` is the main JavaFX application class.

```java
@Override
public void start(Stage stage) throws Exception {
```

- `start` is the method JavaFX automatically calls after launch.
- `Stage` is the main window.

```java
FXMLLoader loader = new FXMLLoader(
        MainApp.class.getResource("/org/example/cryptostockportfoliotracker/portfolio-view.fxml")
);
```

Explanation:

- `FXMLLoader` loads the FXML layout.
- `MainApp.class.getResource(...)` searches inside the resources folder.
- The path is package-aligned, which is cleaner and more maintainable.

```java
Scene scene = new Scene(loader.load(), 800, 600);
```

- `loader.load()` parses the FXML and builds the UI node tree.
- `Scene` is the content container placed inside the window.
- `800, 600` is the initial window size.

```java
stage.setTitle("Portfolio Tracker");
stage.setScene(scene);
stage.show();
```

- `setTitle` changes the window title.
- `setScene` attaches the UI.
- `show` displays the window.

```java
public static void main(String[] args) {
    launch();
}
```

- This is another valid entry point.
- `launch()` starts the JavaFX lifecycle.

## 5.3 `AssetCategory.java`

```java
public enum AssetCategory {
    CRYPTO,
    STOCK
}
```

Explanation:

- An `enum` is used when a value should come from a fixed list.
- Here, the asset can only be one of two categories.
- This is safer than using raw strings like `"crypto"` and `"stock"` because enums reduce typing mistakes.

## 5.4 `TransactionType.java`

```java
public enum TransactionType {
    BUY,
    SELL
}
```

Explanation:

- Another enum.
- A transaction must be either a buy or a sell.
- This is central to the business logic, because the calculations differ depending on the type.

## 5.5 `Asset.java`

Fields:

```java
private final String symbol;
private final String name;
private final AssetCategory category;
```

Meaning:

- `symbol`: the market code, for example `BTC` or `AAPL`
- `name`: the human-readable name, for example `Bitcoin` or `Apple`
- `category`: `CRYPTO` or `STOCK`

Constructor:

```java
public Asset(String symbol, String name, AssetCategory category) {
    this.symbol = symbol.trim().toUpperCase();
    this.name = name.trim();
    this.category = category;
}
```

Why this matters:

- `trim()` removes accidental spaces from user input.
- `toUpperCase()` standardizes symbols so `btc` and `BTC` become the same.
- Standardization is very important because holdings are grouped by symbol later.

Getters:

- `getSymbol()`
- `getName()`
- `getCategory()`

These methods expose the fields safely without making them public.

## 5.6 `Investor.java`

Fields:

```java
private final String name;
private final List<Transaction> transactions = new ArrayList<>();
```

Meaning:

- `name` stores the investor's identity.
- `transactions` stores all transactions for that investor.

Constructor:

```java
public Investor(String name) {
    this.name = name.trim();
}
```

This cleans the input and stores the name.

Methods:

```java
public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
}
```

- Adds a transaction to the investor's history.

```java
public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
}
```

- Returns a read-only view.
- This is good encapsulation because other classes can inspect transactions but cannot modify the internal list directly.

## 5.7 `Transaction.java`

Fields:

- `investorName`
- `asset`
- `type`
- `quantity`
- `unitPrice`
- `timestamp`

This class represents one complete portfolio action.

Important design choice:

- A `Transaction` contains an `Asset` object instead of only storing raw strings.
- This is more object-oriented because the transaction is linked to a proper domain entity.

Constructor:

```java
public Transaction(String investorName, Asset asset, TransactionType type, double quantity, double unitPrice,
                   LocalDateTime timestamp)
```

Why it is important:

- It stores everything necessary to reconstruct the historical event later.
- Because the timestamp is stored, the history remains meaningful after restart.

Convenience getters:

```java
public String getAssetSymbol() {
    return asset.getSymbol();
}
```

and similar methods for name and category.

Why these exist:

- They make JavaFX table binding easier.
- The table can ask the transaction directly for `assetSymbol` instead of navigating through nested objects.

Calculated method:

```java
public double getTotalValue() {
    return quantity * unitPrice;
}
```

This computes the total monetary value of the transaction.

## 5.8 `PortfolioHolding.java`

This is one of the most important classes in the project.

Why it exists:

- `Transaction` shows historical events.
- `PortfolioHolding` shows the current state of one asset after many transactions.

Fields:

- `asset`
- `quantity`
- `averageCost`
- `currentPrice`
- `realizedProfit`

### The key method: `apply(Transaction transaction)`

This method updates the holding using one transaction at a time.

#### If the transaction is `BUY`

```java
double currentCost = quantity * averageCost;
double newCost = transaction.getQuantity() * transaction.getUnitPrice();
quantity += transaction.getQuantity();
averageCost = quantity == 0 ? 0 : (currentCost + newCost) / quantity;
```

Meaning:

- `currentCost` is the cost of the quantity already owned.
- `newCost` is the cost of the new purchase.
- The quantity increases.
- The average cost is recalculated using weighted average cost.

Weighted average formula:

```text
new average cost = (old total cost + new purchase cost) / new total quantity
```

#### If the transaction is `SELL`

```java
quantity -= transaction.getQuantity();
realizedProfit += transaction.getQuantity() * (transaction.getUnitPrice() - averageCost);
```

Meaning:

- The user now owns less quantity.
- The application calculates realized profit from the sold amount.
- Formula:

```text
realized profit = sold quantity × (sell price - average cost)
```

Then:

```java
currentPrice = transaction.getUnitPrice();
```

Why:

- The most recently used price becomes the current reference price for that asset.
- This is a simplification because the app does not yet connect to a live market API.

Other methods:

- `getMarketValue() = quantity * currentPrice`
- `getCostBasis() = quantity * averageCost`
- `getUnrealizedProfit() = marketValue - costBasis`

These are core financial calculations.

## 5.9 `PortfolioSummary.java`

```java
public record PortfolioSummary(
        List<PortfolioHolding> holdings,
        double totalInvested,
        double portfolioValue,
        double realizedProfit,
        double unrealizedProfit
)
```

Why a `record` is used:

- A record is ideal for immutable summary data.
- It automatically generates constructor and accessor methods.

Method:

```java
public double totalProfitOrLoss() {
    return realizedProfit + unrealizedProfit;
}
```

This gives one total number that can be shown in the UI.

## 5.10 `PortfolioFileRepository.java`

This class handles File I/O.

It is responsible for:

- creating the CSV file if needed
- loading transactions from the CSV
- appending new transactions to the CSV

### Header constant

```java
private static final String HEADER = "timestamp,investor,symbol,name,category,type,quantity,unitPrice";
```

This defines the column names in the file.

### `loadTransactions()`

Important steps:

1. `ensureFileExists()` makes sure the file is there.
2. `Files.readAllLines(filePath)` reads every line.
3. The loop starts from index `1`, not `0`.

Why index `1`:

- Index `0` is the header row.

Then:

```java
String[] parts = line.split(",", -1);
```

Meaning:

- Split the CSV row into fields.
- `-1` keeps empty fields instead of dropping them.

Then:

```java
if (parts.length != 8) {
    continue;
}
```

Why:

- Invalid lines are ignored instead of crashing the program.

Then it reconstructs the domain objects:

```java
Asset asset = new Asset(parts[2], parts[3], AssetCategory.valueOf(parts[4]));
```

and then:

```java
Transaction transaction = new Transaction(...)
```

This is important because the file is converted back into proper objects, not just raw strings.

### `appendTransaction(Transaction transaction)`

This method:

1. ensures the file exists
2. builds one CSV line
3. appends that line to the file

Key line:

```java
Files.writeString(filePath, line + System.lineSeparator(), StandardOpenOption.APPEND);
```

Meaning:

- write one line
- add a newline
- append to existing content instead of overwriting it

### `ensureFileExists()`

This method is defensive programming.

It checks:

- if the `data` folder exists
- if the CSV file exists

If not:

- it creates the folder
- it creates the file with the header

### `sanitize(String value)`

```java
return value.replace(",", " ");
```

Why:

- commas would break the CSV structure
- this simple sanitation prevents malformed rows

## 5.11 `PortfolioService.java`

This is the main business logic class.

It connects:

- the `Investor`
- the repository
- the portfolio calculations

### Constructor

```java
public PortfolioService(Investor investor, PortfolioFileRepository repository)
```

Why this is good design:

- dependencies are injected from outside
- the class is easier to test and easier to reuse

### `loadTransactions()`

```java
List<Transaction> storedTransactions = repository.loadTransactions();
for (Transaction transaction : storedTransactions) {
    investor.addTransaction(transaction);
}
```

Meaning:

- the repository reads the file
- the service loads all results into the investor object in memory

### `recordTransaction(...)`

This is another critical method.

Step by step:

1. `validateTransaction(...)` checks the input.
2. A new `Transaction` object is created.
3. If it is a `SELL`, the code checks whether enough quantity exists.
4. If valid, the transaction is:
   - added to the investor
   - written to the CSV file

#### Sell validation

```java
double ownedQuantity = buildSummary().holdings().stream()
        .filter(holding -> holding.getSymbol().equals(asset.getSymbol()))
        .findFirst()
        .map(PortfolioHolding::getQuantity)
        .orElse(0.0);
```

Meaning:

- Build the current holdings.
- Search for the matching asset symbol.
- If found, take its quantity.
- If not found, use `0.0`.

Then:

```java
if (quantity > ownedQuantity) {
    throw new IllegalArgumentException("Not enough quantity available for sale.");
}
```

This prevents impossible sells.

### `getTransactions()`

```java
return new ArrayList<>(investor.getTransactions());
```

Why:

- It returns a copy.
- This protects internal state from accidental external modification.

### `buildSummary()`

This is the heart of the portfolio calculation.

Step by step:

1. Create `holdingsMap`.
2. Create `totalInvested`.
3. Loop through all transactions.
4. For each transaction:
   - create or fetch the correct `PortfolioHolding`
   - if it is a `BUY`, add its value to `totalInvested`
   - apply the transaction to the holding
5. Convert the map into a sorted list.
6. Calculate:
   - `portfolioValue`
   - `realizedProfit`
   - `unrealizedProfit`
7. Return a `PortfolioSummary`

#### Why a map is used

```java
Map<String, PortfolioHolding> holdingsMap = new LinkedHashMap<>();
```

This is used because:

- each asset symbol should map to one holding
- multiple transactions of the same symbol should update the same object

#### Why `computeIfAbsent(...)` is used

```java
PortfolioHolding holding = holdingsMap.computeIfAbsent(
        transaction.getAssetSymbol(),
        key -> new PortfolioHolding(transaction.getAsset())
);
```

Meaning:

- if the symbol already exists, reuse the existing holding
- otherwise create a new holding

This is compact and efficient.

#### Why we filter by quantity

```java
.filter(holding -> holding.getQuantity() > 0)
```

This prevents fully sold assets from showing in current holdings.

### `validateTransaction(...)`

Checks:

- asset symbol and name are not blank
- transaction type is not null
- quantity and price are positive

This keeps the business layer safe even if the UI sends bad input.

## 5.12 `PortfolioController.java`

This file connects the UI and the logic layer.

It is one of the biggest files because it handles:

- JavaFX components
- startup initialization
- user interaction
- table binding
- summary label updates
- error dialogs

### Constant

```java
private static final String DEFAULT_INVESTOR_NAME = "Neph";
```

This stores the default investor used by the app.

### `@FXML` fields

Every `@FXML` field corresponds to an element inside the FXML file.

Examples:

- `TextField assetField`
- `ComboBox<AssetCategory> categoryBox`
- `TableView<Transaction> transactionTable`
- `Label totalInvestedLabel`

Why `@FXML` is needed:

- It tells JavaFX to inject the elements from the FXML file into this controller.

### Observable lists

```java
private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
private final ObservableList<PortfolioHolding> holdings = FXCollections.observableArrayList();
```

Why they are used:

- JavaFX tables update automatically when these lists change.

### Decimal formatting

```java
private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
```

This formats numbers like money values.

### Service creation

```java
private final PortfolioService portfolioService = new PortfolioService(
        new Investor(DEFAULT_INVESTOR_NAME),
        new PortfolioFileRepository(Path.of("data", "transactions.csv"))
);
```

Meaning:

- Create one investor.
- Create one repository pointing to the CSV file.
- Inject both into the service.

### `initialize()`

This method runs automatically after FXML loading.

It does several jobs:

#### Combo box setup

```java
categoryBox.setItems(FXCollections.observableArrayList(AssetCategory.values()));
typeBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
```

This fills the combo boxes with enum values.

#### Default selections

```java
categoryBox.setValue(AssetCategory.CRYPTO);
typeBox.setValue(TransactionType.BUY);
investorField.setText(DEFAULT_INVESTOR_NAME);
```

This gives the UI valid starting values.

#### Table setup

```java
configureTransactionTable();
configureHoldingsTable();
```

These methods define which property each column will show.

#### Bind lists to tables

```java
transactionTable.setItems(transactions);
holdingsTable.setItems(holdings);
```

This is how the tables know where their data comes from.

#### Load persisted data

```java
transactions.setAll(portfolioService.loadTransactions());
refreshSummary();
```

Meaning:

- load all saved transactions
- put them in the table list
- rebuild the summary labels and holdings

### `addTransaction()`

This method runs when the button is clicked.

Detailed flow:

1. Read text from fields.
2. Parse quantity and price.
3. Read selected category and type.
4. Create `Asset`.
5. Call service to record transaction.
6. Refresh the transaction table.
7. Refresh the summary.
8. Clear form fields.

Important line:

```java
double quantity = Double.parseDouble(quantityField.getText());
```

Why it matters:

- user input comes in as text
- calculations require numbers

Potential issue to mention in presentation:

- if the user types invalid text, parsing fails
- this is caught by exception handling

### Error handling

```java
catch (IllegalArgumentException e) {
    showError("Invalid transaction", e.getMessage());
} catch (IOException e) {
    showError("Could not save transaction.", e.getMessage());
}
```

This separates:

- business validation errors
- file operation errors

That is good design because the messages are more meaningful.

### `configureTransactionTable()`

This method binds each transaction column to a property name.

Example:

```java
txSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("assetSymbol"));
```

JavaFX will search for:

```java
getAssetSymbol()
```

inside the `Transaction` class.

That is why those convenience getters were added.

Special case:

```java
txDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
        cell.getValue().getTimestamp().toLocalDate().toString()
));
```

Why this one is different:

- the date needs transformation
- the stored field is `LocalDateTime`
- the UI only wants the date text

### `configureHoldingsTable()`

Same idea as the transaction table, but for current holdings.

Examples:

- `symbol`
- `name`
- `averageCost`
- `marketValue`
- `unrealizedProfit`

### `refreshSummary()`

This method updates the bottom-right summary section.

```java
PortfolioSummary summary = portfolioService.buildSummary();
```

This recalculates the full portfolio state.

Then:

```java
holdings.setAll(summary.holdings());
```

This refreshes the holdings table.

Then all labels are updated using formatted currency values.

### `formatCurrency(double value)`

This is a helper method:

```java
return "$" + decimalFormat.format(value);
```

It keeps formatting logic in one place.

### `showError(...)`

This method creates a JavaFX `Alert`.

Why it is useful:

- keeps dialog code out of the rest of the methods
- makes the controller cleaner

## 5.13 `portfolio-view.fxml`

This file defines the layout of the interface.

Important top-level line:

```xml
<BorderPane
    fx:controller="org.example.cryptostockportfoliotracker.controller.PortfolioController"
    stylesheets="@portfolio-theme.css"
    styleClass="app-root">
```

Meaning:

- `BorderPane` is the main layout container
- `fx:controller` links this FXML file to `PortfolioController`
- `stylesheets` attaches the external CSS file
- `styleClass` applies root styling

### Why `BorderPane` was used

Because it naturally separates the UI into sections:

- top: form/header
- center: main content

### Top area

The top area contains:

- title
- subtitle
- transaction input form

The form uses `GridPane` because grid layout is ideal for aligned labels and input fields.

### `fx:id`

Example:

```xml
<TextField fx:id="assetField" ... />
```

This must match:

```java
@FXML private TextField assetField;
```

in the controller.

If names do not match, injection fails.

### Button event binding

```xml
<Button text="Record Transaction" onAction="#addTransaction" ... />
```

Meaning:

- when the button is clicked
- JavaFX calls `addTransaction()` in the controller

### Tables

There are two tables:

1. `transactionTable`
2. `holdingsTable`

This directly supports the assignment requirement of visualization.

### Summary card

The labels:

- `totalInvestedLabel`
- `portfolioValueLabel`
- `realizedProfitLabel`
- `unrealizedProfitLabel`
- `totalProfitLossLabel`

are placeholders that the controller updates dynamically.

## 5.14 `portfolio-theme.css`

This file improves appearance and keeps styling separate from structure.

That separation is important:

- FXML = structure
- CSS = visual design

### `.root.app-root`

Defines the general font and background of the application.

### `.hero-panel`

Styles the top banner:

- padding
- gradient
- bottom border

### `.form-grid`, `.form-label`, `.form-input`

These style the input section.

Important idea:

- the form remains readable
- all input fields look consistent
- CSS avoids repeating long inline styles

### `.primary-button`

Gives the button:

- gradient background
- rounded corners
- bold white text
- hover effect

### `.panel`, `.alt-panel`

These style the two main content cards.

### `.table-view.data-table`

Customizes the JavaFX tables so they do not look like default JavaFX controls.

### `.summary-card`

Styles the summary metrics section so it looks like a separate information block.

## 5.15 `pom.xml`

This file defines the Maven project configuration.

### Coordinates

```xml
<groupId>org.example</groupId>
<artifactId>portfolio-tracker</artifactId>
<version>1.0-SNAPSHOT</version>
```

These identify the project.

### Java version

```xml
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
```

This tells Maven to compile with Java 17.

### Dependencies

```xml
<artifactId>javafx-controls</artifactId>
<artifactId>javafx-fxml</artifactId>
```

Why both are needed:

- `javafx-controls` provides UI controls like buttons, labels, tables, text fields
- `javafx-fxml` provides FXML loading support

### Plugins

`maven-compiler-plugin`:

- compiles the Java source code

`javafx-maven-plugin`:

- helps run the JavaFX application with Maven

Key line:

```xml
<mainClass>org.example.cryptostockportfoliotracker.MainApp</mainClass>
```

This tells Maven which class to launch.

## 6. Important OOP Decisions

## Why not store everything inside the controller?

Because that would create a "God class".

Instead:

- models store data
- services contain logic
- controller handles UI behavior

This is cleaner, easier to debug, and more object-oriented.

## Why use enums?

Because `BUY/SELL` and `CRYPTO/STOCK` are fixed choices.

Enums:

- make the code safer
- reduce string mistakes
- improve readability

## Why use a repository class?

Because file operations should not be mixed directly with UI code.

This separation means:

- the controller stays cleaner
- persistence can be changed later
- the service layer remains focused on business logic

## 7. Profit/Loss Logic Explained Carefully

There are three major calculations:

### Total invested

Only `BUY` transactions increase this value.

Formula:

```text
total invested = sum of all buy transaction totals
```

### Realized profit

This is profit already locked in by selling.

Formula:

```text
realized profit = sold quantity × (sell price - average cost)
```

### Unrealized profit

This is profit on the quantity still held.

Formula:

```text
unrealized profit = current market value - remaining cost basis
```

Where:

```text
current market value = quantity × current price
cost basis = quantity × average cost
```

### Total profit/loss

```text
total profit/loss = realized profit + unrealized profit
```

## 8. Why The Current Price Works This Way

This project does not use a live API.

So the "current price" of each asset is approximated using the most recent transaction price for that asset.

That means:

- if the last action was a buy at 100, current price becomes 100
- if the last action was a sell at 120, current price becomes 120

This is acceptable for an academic OOP/File I/O project, but in a real production system you would normally call a market price API.

## 9. File I/O Requirement

The assignment explicitly asks for transaction history to be stored in a file.

This is fully satisfied because:

- transactions are appended to `data/transactions.csv`
- old transactions are reloaded when the program starts
- data remains even after the app closes

Example CSV row:

```text
2026-03-23T15:20:00,Neph,BTC,Bitcoin,CRYPTO,BUY,1.5,64000.0
```

## 10. Strengths of the Current Implementation

- clear OOP separation
- correct use of model/service/controller layers
- persistent transaction history
- support for both crypto and stock assets
- validation against invalid sells
- readable UI with external CSS
- JavaFX tables for required visualization

## 11. Limitations You Can Mention Honestly

No student project is perfect. You can mention these as future improvements:

- only one default investor is currently used
- no live market API
- CSV is simple but not ideal for very large data
- no unit tests yet
- no edit/delete transaction feature
- no charts yet

This is actually a good presentation point because it shows critical thinking.

## 12. Possible Viva/Presentation Questions And Good Answers

### Why did you use OOP here?

Because the problem naturally contains entities such as investor, asset, transaction, and portfolio holding. OOP allows each entity to have clear responsibilities and makes the code easier to extend.

### Why did you separate the service and repository layers?

To avoid mixing UI logic, business logic, and file operations in one class. This improves maintainability and follows good software design.

### Why did you use enums?

Because the possible values for asset category and transaction type are fixed and known in advance.

### How is the portfolio value calculated?

The current portfolio value is the sum of `quantity × currentPrice` for all active holdings.

### How is realized profit different from unrealized profit?

Realized profit comes from assets already sold. Unrealized profit comes from assets still owned.

## 13. How To Present This Project

A good explanation order in your demo is:

1. Start with the assignment requirements.
2. Show the UI.
3. Explain the OOP classes.
4. Explain file persistence.
5. Explain profit/loss formulas.
6. Show how controller, service, and model communicate.
7. Mention future improvements.

## 14. Final Summary

This project is a solid JavaFX OOP desktop application because it includes:

- domain classes for the portfolio problem
- transaction management
- CSV file persistence
- portfolio calculations
- JavaFX visualization
- external CSS styling

If you want, a next improvement would be to create:

- a third markdown file for your oral presentation script
- a UML class diagram
- a sequence diagram showing the transaction flow
- inline code comments inside the Java files for study purposes

## 15. Extra Close Code Walkthrough

This section is intentionally more detailed, so you can explain the code more confidently in front of your lecturer.

## 15.1 `MainApp.java` line-by-line logic

```java
public class MainApp extends Application {
```

- The class inherits from `Application`.
- This is required for a JavaFX application window to exist.

```java
@Override
public void start(Stage stage) throws Exception {
```

- `@Override` means this method replaces the inherited JavaFX lifecycle method.
- `Stage` is the top-level desktop window.
- `throws Exception` means any loading failure can propagate upward.

```java
FXMLLoader loader = new FXMLLoader(
        MainApp.class.getResource("/org/example/cryptostockportfoliotracker/portfolio-view.fxml")
);
```

- `FXMLLoader` is the JavaFX class that parses the XML layout.
- `MainApp.class.getResource(...)` looks inside the compiled resources folder.
- The leading `/` means the path starts from the classpath root.

```java
Scene scene = new Scene(loader.load(), 800, 600);
```

- `loader.load()` builds the UI tree from the FXML file.
- `Scene` wraps that tree so it can be placed into the window.
- `800, 600` is the initial size, not a hard limit.

```java
stage.setTitle("Portfolio Tracker");
stage.setScene(scene);
stage.show();
```

- `setTitle` changes the OS window title.
- `setScene` places the content into the window.
- `show` finally renders the application.

## 15.2 `PortfolioController.java` line-by-line logic

This is the most important file to understand because it connects the user interface with the model and service layers.

### Constant and injected controls

```java
private static final String DEFAULT_INVESTOR_NAME = "Neph";
```

- `static final` means it is a constant.
- It is used to avoid repeating the same hardcoded investor name.

Then many lines like:

```java
@FXML private TextField assetField;
@FXML private ComboBox<TransactionType> typeBox;
@FXML private TableView<Transaction> transactionTable;
```

Meaning:

- `@FXML` tells JavaFX that these fields are connected to nodes declared in the FXML file.
- If the `fx:id` in the FXML does not match these field names, JavaFX injection fails.

### Local state

```java
private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
private final ObservableList<PortfolioHolding> holdings = FXCollections.observableArrayList();
```

- `ObservableList` is used because JavaFX tables listen to it.
- When the list changes, the table can refresh automatically.

```java
private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
```

- This formats doubles into readable money-like values.

```java
private final PortfolioService portfolioService = new PortfolioService(
        new Investor(DEFAULT_INVESTOR_NAME),
        new PortfolioFileRepository(Path.of("data", "transactions.csv"))
);
```

- A service object is created once for the controller.
- It receives two dependencies:
  - an `Investor`
  - a file repository
- `Path.of("data", "transactions.csv")` builds a platform-safe path to the CSV file.

### `initialize()`

```java
categoryBox.setItems(FXCollections.observableArrayList(AssetCategory.values()));
typeBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
```

- `AssetCategory.values()` returns all enum values.
- `TransactionType.values()` returns `BUY` and `SELL`.
- These arrays are converted into observable lists for the combo boxes.

```java
categoryBox.setValue(AssetCategory.CRYPTO);
typeBox.setValue(TransactionType.BUY);
```

- These lines define default selections so the form is never empty at startup.

```java
investorField.setText(DEFAULT_INVESTOR_NAME);
```

- The investor field is displayed in the UI.
- It is read-only, so it acts like an informational field.

```java
configureTransactionTable();
configureHoldingsTable();
```

- These methods separate table-configuration logic from the initialization flow.
- This keeps `initialize()` easier to read.

```java
transactionTable.setItems(transactions);
holdingsTable.setItems(holdings);
```

- These lines bind the tables to the observable lists.
- After that, any updates to the lists can appear in the UI.

```java
transactions.setAll(portfolioService.loadTransactions());
refreshSummary();
```

- `loadTransactions()` reads previously saved data from disk.
- `setAll(...)` replaces the current list contents with the loaded items.
- `refreshSummary()` rebuilds holdings and summary labels.

### `addTransaction()`

```java
String assetSymbol = assetField.getText().trim();
String assetName = assetNameField.getText().trim();
```

- The controller reads raw text from the input fields.
- `trim()` avoids leading and trailing whitespace issues.

```java
double quantity = Double.parseDouble(quantityField.getText());
double price = Double.parseDouble(priceField.getText());
```

- Text is converted into numeric values.
- If the user types invalid text, `parseDouble` throws an exception.

```java
AssetCategory category = categoryBox.getValue();
TransactionType type = typeBox.getValue();
```

- The selected enum values are read from the combo boxes.

```java
Asset asset = new Asset(assetSymbol, assetName, category);
```

- The controller creates a domain object instead of passing loose strings everywhere.

```java
portfolioService.recordTransaction(asset, type, quantity, price);
```

- This is the key handoff from UI layer to business layer.
- After this line, validation, sell checks, and CSV storage are handled in the service layer.

```java
transactions.setAll(portfolioService.getTransactions());
refreshSummary();
```

- After saving, the controller refreshes the current UI state from the service.

```java
assetNameField.clear();
assetField.clear();
quantityField.clear();
priceField.clear();
```

- These lines reset the form after a successful transaction.

### Table configuration methods

Example:

```java
txSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("assetSymbol"));
```

- JavaFX looks for a getter named `getAssetSymbol()` in the `Transaction` class.
- That return value is displayed in the column.

Example:

```java
holdingPnlColumn.setCellValueFactory(new PropertyValueFactory<>("unrealizedProfit"));
```

- JavaFX looks for `getUnrealizedProfit()` in `PortfolioHolding`.

Special case:

```java
txDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
        cell.getValue().getTimestamp().toLocalDate().toString()
));
```

- A lambda is used because the date requires custom transformation.
- The stored value is `LocalDateTime`.
- The UI only displays the date part as text.

### `refreshSummary()`

```java
PortfolioSummary summary = portfolioService.buildSummary();
```

- The controller asks the service to compute the full portfolio summary from the current transactions.

```java
holdings.setAll(summary.holdings());
```

- The holdings table is updated from the computed summary.

```java
totalInvestedLabel.setText("Total Invested: " + formatCurrency(summary.totalInvested()));
```

- This converts the numeric value into a readable label for the UI.
- The same pattern is repeated for the other summary labels.

## 15.3 `PortfolioService.java` line-by-line logic

This file is the computational center of the project.

### Constructor

```java
public PortfolioService(Investor investor, PortfolioFileRepository repository) {
    this.investor = investor;
    this.repository = repository;
}
```

- The service does not create its own dependencies internally.
- This is a cleaner design because dependencies are injected from outside.

### `loadTransactions()`

```java
List<Transaction> storedTransactions = repository.loadTransactions();
```

- Ask the repository to read the CSV file.

```java
for (Transaction transaction : storedTransactions) {
    investor.addTransaction(transaction);
}
```

- Every loaded transaction is inserted into the investor's in-memory transaction list.

### `recordTransaction(...)`

```java
validateTransaction(asset, type, quantity, unitPrice);
```

- Before doing anything, the method checks whether the transaction data is valid.

```java
Transaction transaction = new Transaction(
        investor.getName(),
        asset,
        type,
        quantity,
        unitPrice,
        LocalDateTime.now()
);
```

- A transaction object is created with the current timestamp.
- `LocalDateTime.now()` records the exact moment of creation.

```java
if (type == TransactionType.SELL) {
```

- Sell transactions need extra validation because you cannot sell what you do not own.

```java
double ownedQuantity = buildSummary().holdings().stream()
```

- The service computes the current portfolio summary first.
- Then it searches the holdings to find how much of that asset is currently owned.

```java
.filter(holding -> holding.getSymbol().equals(asset.getSymbol()))
```

- Keep only the holding that matches the asset symbol of the requested sale.

```java
.map(PortfolioHolding::getQuantity)
.orElse(0.0);
```

- If the holding exists, extract its quantity.
- If it does not exist, treat the owned quantity as zero.

```java
if (quantity > ownedQuantity) {
    throw new IllegalArgumentException("Not enough quantity available for sale.");
}
```

- This prevents invalid portfolio states.

```java
investor.addTransaction(transaction);
repository.appendTransaction(transaction);
```

- First update in-memory state.
- Then persist the new transaction to disk.

### `buildSummary()`

```java
Map<String, PortfolioHolding> holdingsMap = new LinkedHashMap<>();
double totalInvested = 0;
```

- The map groups transactions by asset symbol.
- `totalInvested` accumulates the total amount spent on purchases.

```java
for (Transaction transaction : investor.getTransactions()) {
```

- The method rebuilds the whole portfolio state from transaction history.
- This is a very clean approach because the summary is derived from the source of truth.

```java
PortfolioHolding holding = holdingsMap.computeIfAbsent(
        transaction.getAssetSymbol(),
        key -> new PortfolioHolding(transaction.getAsset())
);
```

- If the asset already has a holding, reuse it.
- Otherwise create a new holding for that asset.

```java
if (transaction.getType() == TransactionType.BUY) {
    totalInvested += transaction.getTotalValue();
}
```

- Only buys increase invested capital.

```java
holding.apply(transaction);
```

- This delegates the actual holding update to the `PortfolioHolding` class.
- That is good object-oriented design because each class handles its own responsibility.

```java
List<PortfolioHolding> holdings = holdingsMap.values().stream()
        .filter(holding -> holding.getQuantity() > 0)
        .sorted(Comparator.comparing(PortfolioHolding::getSymbol))
        .collect(Collectors.toList());
```

- Convert the map into a list.
- Remove fully sold assets.
- Sort alphabetically by symbol.

```java
double portfolioValue = holdings.stream().mapToDouble(PortfolioHolding::getMarketValue).sum();
double realizedProfit = holdingsMap.values().stream().mapToDouble(PortfolioHolding::getRealizedProfit).sum();
double unrealizedProfit = holdings.stream().mapToDouble(PortfolioHolding::getUnrealizedProfit).sum();
```

- Each total is calculated from the generated holdings.

```java
return new PortfolioSummary(holdings, totalInvested, portfolioValue, realizedProfit, unrealizedProfit);
```

- The final result is returned as one immutable summary object.

## 15.4 `PortfolioFileRepository.java` line-by-line logic

```java
private static final String HEADER = "timestamp,investor,symbol,name,category,type,quantity,unitPrice";
```

- This stores the CSV header in one place.

```java
private final Path filePath;
```

- The repository stores the target file path as state.

```java
public PortfolioFileRepository(Path filePath) {
    this.filePath = filePath;
}
```

- The path is supplied externally, which keeps the class flexible.

### `loadTransactions()`

```java
ensureFileExists();
```

- Before reading, the code guarantees that the file exists.

```java
List<String> lines = Files.readAllLines(filePath);
```

- Reads the entire CSV into memory as lines of text.

```java
for (int i = 1; i < lines.size(); i++) {
```

- Start at line 1 instead of 0 because line 0 is the header row.

```java
String line = lines.get(i).trim();
if (line.isEmpty()) {
    continue;
}
```

- Ignore empty lines.

```java
String[] parts = line.split(",", -1);
if (parts.length != 8) {
    continue;
}
```

- Split the CSV row into exactly 8 values.
- Invalid rows are skipped rather than crashing the whole app.

```java
Asset asset = new Asset(parts[2], parts[3], AssetCategory.valueOf(parts[4]));
```

- Recreate the `Asset` object from the CSV values.

```java
Transaction transaction = new Transaction(
        parts[1],
        asset,
        TransactionType.valueOf(parts[5]),
        Double.parseDouble(parts[6]),
        Double.parseDouble(parts[7]),
        LocalDateTime.parse(parts[0])
);
```

- Recreate the `Transaction` object using parsed values from the CSV row.

### `appendTransaction(...)`

```java
String line = String.join(",",
```

- Build one CSV row from the transaction data.

```java
Files.writeString(filePath, line + System.lineSeparator(), StandardOpenOption.APPEND);
```

- Append the row to the file instead of rewriting the whole file.

### `ensureFileExists()`

```java
if (Files.notExists(filePath.getParent())) {
    Files.createDirectories(filePath.getParent());
}
```

- If the `data` folder does not exist, create it.

```java
if (Files.notExists(filePath)) {
    Files.writeString(filePath, HEADER + System.lineSeparator(), StandardOpenOption.CREATE);
}
```

- If the CSV file does not exist, create it and place the header inside.

## 15.5 How the FXML and controller connect exactly

Example from FXML:

```xml
<TextField fx:id="assetField" promptText="BTC / AAPL" styleClass="form-input" />
```

Matching controller line:

```java
@FXML private TextField assetField;
```

Meaning:

- JavaFX reads the `fx:id`.
- It searches in the controller for a field with the same name.
- It injects the UI node into that field.

Another example:

```xml
<Button text="Record Transaction" onAction="#addTransaction" />
```

Matching controller method:

```java
@FXML
public void addTransaction() {
```

Meaning:

- When the user clicks the button, JavaFX calls `addTransaction()`.

## 15.6 How to answer if someone asks “why not explain literally every single line?”

A good honest answer is:

"I can explain every line if needed, but in software engineering the most important thing is to understand each line's role in the larger architecture. So I grouped the explanation into object model, business logic, persistence, UI binding, and calculations, then zoomed into the important lines inside each file."

That answer sounds mature and technically strong.
