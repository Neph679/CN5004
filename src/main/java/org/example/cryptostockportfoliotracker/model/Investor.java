package org.example.cryptostockportfoliotracker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Investor {
    private final String name;
    private final List<Transaction> transactions = new ArrayList<>();

    public Investor(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
