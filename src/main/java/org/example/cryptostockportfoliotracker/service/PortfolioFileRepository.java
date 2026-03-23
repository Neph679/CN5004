package org.example.cryptostockportfoliotracker.service;

import org.example.cryptostockportfoliotracker.model.Asset;
import org.example.cryptostockportfoliotracker.model.AssetCategory;
import org.example.cryptostockportfoliotracker.model.Transaction;
import org.example.cryptostockportfoliotracker.model.TransactionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PortfolioFileRepository {
    private static final String HEADER = "timestamp,investor,symbol,name,category,type,quantity,unitPrice";
    private final Path filePath;

    public PortfolioFileRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<Transaction> loadTransactions() throws IOException {
        ensureFileExists();
        List<String> lines = Files.readAllLines(filePath);
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length != 8) {
                continue;
            }

            Asset asset = new Asset(parts[2], parts[3], AssetCategory.valueOf(parts[4]));
            Transaction transaction = new Transaction(
                    parts[1],
                    asset,
                    TransactionType.valueOf(parts[5]),
                    Double.parseDouble(parts[6]),
                    Double.parseDouble(parts[7]),
                    LocalDateTime.parse(parts[0])
            );
            transactions.add(transaction);
        }
        return transactions;
    }

    public void appendTransaction(Transaction transaction) throws IOException {
        ensureFileExists();
        String line = String.join(",",
                transaction.getTimestamp().toString(),
                sanitize(transaction.getInvestorName()),
                sanitize(transaction.getAssetSymbol()),
                sanitize(transaction.getAssetName()),
                transaction.getAssetCategory().name(),
                transaction.getType().name(),
                String.valueOf(transaction.getQuantity()),
                String.valueOf(transaction.getUnitPrice())
        );
        Files.writeString(filePath, line + System.lineSeparator(), StandardOpenOption.APPEND);
    }

    private void ensureFileExists() throws IOException {
        if (Files.notExists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        if (Files.notExists(filePath)) {
            Files.writeString(filePath, HEADER + System.lineSeparator(), StandardOpenOption.CREATE);
        }
    }

    private String sanitize(String value) {
        return value.replace(",", " ");
    }
}
