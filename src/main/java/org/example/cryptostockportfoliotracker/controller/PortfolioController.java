package org.example.cryptostockportfoliotracker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.cryptostockportfoliotracker.model.Transaction;

public class PortfolioController {

    @FXML private TextField assetField;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TableView<Transaction> transactionTable;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList("BUY", "SELL"));
        transactionTable.setItems(transactions);
    }

    @FXML
    public void addTransaction() {
        try {
            String asset = assetField.getText();
            double quantity = Double.parseDouble(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            String type = typeBox.getValue();

            Transaction t = new Transaction(asset, type, quantity, price);
            transactions.add(t);

            assetField.clear();
            quantityField.clear();
            priceField.clear();

        } catch (Exception e) {
            System.out.println("Error");
        }
    }
}