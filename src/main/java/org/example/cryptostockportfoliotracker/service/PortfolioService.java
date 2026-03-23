package org.example.cryptostockportfoliotracker.service;

import org.example.cryptostockportfoliotracker.model.Asset;
import org.example.cryptostockportfoliotracker.model.Investor;
import org.example.cryptostockportfoliotracker.model.PortfolioHolding;
import org.example.cryptostockportfoliotracker.model.PortfolioSummary;
import org.example.cryptostockportfoliotracker.model.Transaction;
import org.example.cryptostockportfoliotracker.model.TransactionType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortfolioService {
    private final Investor investor;
    private final PortfolioFileRepository repository;

    public PortfolioService(Investor investor, PortfolioFileRepository repository) {
        this.investor = investor;
        this.repository = repository;
    }

    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> storedTransactions = repository.loadTransactions();
        for (Transaction transaction : storedTransactions) {
            investor.addTransaction(transaction);
        }
        return investor.getTransactions();
    }

    public Transaction recordTransaction(Asset asset, TransactionType type, double quantity, double unitPrice)
            throws IOException {
        validateTransaction(asset, type, quantity, unitPrice);

        Transaction transaction = new Transaction(
                investor.getName(),
                asset,
                type,
                quantity,
                unitPrice,
                LocalDateTime.now()
        );

        if (type == TransactionType.SELL) {
            double ownedQuantity = buildSummary().holdings().stream()
                    .filter(holding -> holding.getSymbol().equals(asset.getSymbol()))
                    .findFirst()
                    .map(PortfolioHolding::getQuantity)
                    .orElse(0.0);
            if (quantity > ownedQuantity) {
                throw new IllegalArgumentException("Not enough quantity available for sale.");
            }
        }

        investor.addTransaction(transaction);
        repository.appendTransaction(transaction);
        return transaction;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(investor.getTransactions());
    }

    public PortfolioSummary buildSummary() {
        Map<String, PortfolioHolding> holdingsMap = new LinkedHashMap<>();
        double totalInvested = 0;

        for (Transaction transaction : investor.getTransactions()) {
            PortfolioHolding holding = holdingsMap.computeIfAbsent(
                    transaction.getAssetSymbol(),
                    key -> new PortfolioHolding(transaction.getAsset())
            );

            if (transaction.getType() == TransactionType.BUY) {
                totalInvested += transaction.getTotalValue();
            }

            holding.apply(transaction);
        }

        List<PortfolioHolding> holdings = holdingsMap.values().stream()
                .filter(holding -> holding.getQuantity() > 0)
                .sorted(Comparator.comparing(PortfolioHolding::getSymbol))
                .collect(Collectors.toList());

        double portfolioValue = holdings.stream().mapToDouble(PortfolioHolding::getMarketValue).sum();
        double realizedProfit = holdingsMap.values().stream().mapToDouble(PortfolioHolding::getRealizedProfit).sum();
        double unrealizedProfit = holdings.stream().mapToDouble(PortfolioHolding::getUnrealizedProfit).sum();

        return new PortfolioSummary(holdings, totalInvested, portfolioValue, realizedProfit, unrealizedProfit);
    }

    private void validateTransaction(Asset asset, TransactionType type, double quantity, double unitPrice) {
        if (asset.getSymbol().isBlank() || asset.getName().isBlank()) {
            throw new IllegalArgumentException("Asset name and symbol are required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        if (quantity <= 0 || unitPrice <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive numbers.");
        }
    }
}
