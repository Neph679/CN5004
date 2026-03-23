package org.example.cryptostockportfoliotracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cryptostockportfoliotracker.model.Asset;
import org.example.cryptostockportfoliotracker.model.AssetCategory;
import org.example.cryptostockportfoliotracker.model.Investor;
import org.example.cryptostockportfoliotracker.model.PortfolioHolding;
import org.example.cryptostockportfoliotracker.model.PortfolioSummary;
import org.example.cryptostockportfoliotracker.model.Transaction;
import org.example.cryptostockportfoliotracker.model.TransactionType;
import org.example.cryptostockportfoliotracker.service.PortfolioFileRepository;
import org.example.cryptostockportfoliotracker.service.PortfolioService;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

public class PortfolioController {
    private static final String DEFAULT_INVESTOR_NAME = "Neph";

    @FXML private TextField investorField;
    @FXML private TextField assetNameField;
    @FXML private TextField assetField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private ComboBox<AssetCategory> categoryBox;
    @FXML private ComboBox<TransactionType> typeBox;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> txDateColumn;
    @FXML private TableColumn<Transaction, String> txSymbolColumn;
    @FXML private TableColumn<Transaction, String> txNameColumn;
    @FXML private TableColumn<Transaction, AssetCategory> txCategoryColumn;
    @FXML private TableColumn<Transaction, TransactionType> txTypeColumn;
    @FXML private TableColumn<Transaction, Double> txQuantityColumn;
    @FXML private TableColumn<Transaction, Double> txPriceColumn;
    @FXML private TableColumn<Transaction, Double> txTotalColumn;

    @FXML private TableView<PortfolioHolding> holdingsTable;
    @FXML private TableColumn<PortfolioHolding, String> holdingSymbolColumn;
    @FXML private TableColumn<PortfolioHolding, String> holdingNameColumn;
    @FXML private TableColumn<PortfolioHolding, AssetCategory> holdingCategoryColumn;
    @FXML private TableColumn<PortfolioHolding, Double> holdingQuantityColumn;
    @FXML private TableColumn<PortfolioHolding, Double> holdingAvgCostColumn;
    @FXML private TableColumn<PortfolioHolding, Double> holdingCurrentPriceColumn;
    @FXML private TableColumn<PortfolioHolding, Double> holdingValueColumn;
    @FXML private TableColumn<PortfolioHolding, Double> holdingPnlColumn;

    @FXML private Label totalInvestedLabel;
    @FXML private Label portfolioValueLabel;
    @FXML private Label realizedProfitLabel;
    @FXML private Label unrealizedProfitLabel;
    @FXML private Label totalProfitLossLabel;

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<PortfolioHolding> holdings = FXCollections.observableArrayList();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final PortfolioService portfolioService = new PortfolioService(
            new Investor(DEFAULT_INVESTOR_NAME),
            new PortfolioFileRepository(Path.of("data", "transactions.csv"))
    );

    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(AssetCategory.values()));
        typeBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        categoryBox.setValue(AssetCategory.CRYPTO);
        typeBox.setValue(TransactionType.BUY);
        investorField.setText(DEFAULT_INVESTOR_NAME);

        configureTransactionTable();
        configureHoldingsTable();
        transactionTable.setItems(transactions);
        holdingsTable.setItems(holdings);

        try {
            transactions.setAll(portfolioService.loadTransactions());
            refreshSummary();
        } catch (IOException e) {
            showError("Could not load transaction history.", e.getMessage());
        }
    }

    @FXML
    public void addTransaction() {
        try {
            String assetSymbol = assetField.getText().trim();
            String assetName = assetNameField.getText().trim();
            double quantity = Double.parseDouble(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            AssetCategory category = categoryBox.getValue();
            TransactionType type = typeBox.getValue();

            Asset asset = new Asset(assetSymbol, assetName, category);
            portfolioService.recordTransaction(asset, type, quantity, price);
            transactions.setAll(portfolioService.getTransactions());
            refreshSummary();

            assetNameField.clear();
            assetField.clear();
            quantityField.clear();
            priceField.clear();
        } catch (IllegalArgumentException e) {
            showError("Invalid transaction", e.getMessage());
        } catch (IOException e) {
            showError("Could not save transaction.", e.getMessage());
        }
    }

    private void configureTransactionTable() {
        txDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTimestamp().toLocalDate().toString()
        ));
        txSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("assetSymbol"));
        txNameColumn.setCellValueFactory(new PropertyValueFactory<>("assetName"));
        txCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("assetCategory"));
        txTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        txQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        txPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        txTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
    }

    private void configureHoldingsTable() {
        holdingSymbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        holdingNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        holdingCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        holdingQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        holdingAvgCostColumn.setCellValueFactory(new PropertyValueFactory<>("averageCost"));
        holdingCurrentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        holdingValueColumn.setCellValueFactory(new PropertyValueFactory<>("marketValue"));
        holdingPnlColumn.setCellValueFactory(new PropertyValueFactory<>("unrealizedProfit"));
    }

    private void refreshSummary() {
        PortfolioSummary summary = portfolioService.buildSummary();
        holdings.setAll(summary.holdings());
        totalInvestedLabel.setText("Total Invested: " + formatCurrency(summary.totalInvested()));
        portfolioValueLabel.setText("Portfolio Value: " + formatCurrency(summary.portfolioValue()));
        realizedProfitLabel.setText("Realized P/L: " + formatCurrency(summary.realizedProfit()));
        unrealizedProfitLabel.setText("Unrealized P/L: " + formatCurrency(summary.unrealizedProfit()));
        totalProfitLossLabel.setText("Total P/L: " + formatCurrency(summary.totalProfitOrLoss()));
    }

    private String formatCurrency(double value) {
        return "$" + decimalFormat.format(value);
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Portfolio Tracker");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
