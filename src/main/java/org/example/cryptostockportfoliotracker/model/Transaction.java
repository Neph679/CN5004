package org.example.cryptostockportfoliotracker.model;

import java.time.LocalDateTime;

public class Transaction {
    private final String investorName;
    private final Asset asset;
    private final TransactionType type;
    private final double quantity;
    private final double unitPrice;
    private final LocalDateTime timestamp;

    public Transaction(String investorName, Asset asset, TransactionType type, double quantity, double unitPrice,
                       LocalDateTime timestamp) {
        this.investorName = investorName;
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.timestamp = timestamp;
    }

    public String getInvestorName() {
        return investorName;
    }

    public Asset getAsset() {
        return asset;
    }

    public String getAssetSymbol() {
        return asset.getSymbol();
    }

    public String getAssetName() {
        return asset.getName();
    }

    public AssetCategory getAssetCategory() {
        return asset.getCategory();
    }

    public TransactionType getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getTotalValue() {
        return quantity * unitPrice;
    }
}
