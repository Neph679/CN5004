package org.example.cryptostockportfoliotracker.model;

public class PortfolioHolding {
    private final Asset asset;
    private double quantity;
    private double averageCost;
    private double currentPrice;
    private double realizedProfit;

    public PortfolioHolding(Asset asset) {
        this.asset = asset;
    }

    public void apply(Transaction transaction) {
        if (transaction.getType() == TransactionType.BUY) {
            double currentCost = quantity * averageCost;
            double newCost = transaction.getQuantity() * transaction.getUnitPrice();
            quantity += transaction.getQuantity();
            averageCost = quantity == 0 ? 0 : (currentCost + newCost) / quantity;
        } else {
            quantity -= transaction.getQuantity();
            realizedProfit += transaction.getQuantity() * (transaction.getUnitPrice() - averageCost);
        }
        currentPrice = transaction.getUnitPrice();
    }

    public Asset getAsset() {
        return asset;
    }

    public String getSymbol() {
        return asset.getSymbol();
    }

    public String getName() {
        return asset.getName();
    }

    public AssetCategory getCategory() {
        return asset.getCategory();
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAverageCost() {
        return averageCost;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getMarketValue() {
        return quantity * currentPrice;
    }

    public double getCostBasis() {
        return quantity * averageCost;
    }

    public double getUnrealizedProfit() {
        return getMarketValue() - getCostBasis();
    }

    public double getRealizedProfit() {
        return realizedProfit;
    }
}
