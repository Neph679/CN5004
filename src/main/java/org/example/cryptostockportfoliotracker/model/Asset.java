package org.example.cryptostockportfoliotracker.model;

public class Asset {
    private final String symbol;
    private final String name;
    private final AssetCategory category;

    public Asset(String symbol, String name, AssetCategory category) {
        this.symbol = symbol.trim().toUpperCase();
        this.name = name.trim();
        this.category = category;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public AssetCategory getCategory() {
        return category;
    }
}
