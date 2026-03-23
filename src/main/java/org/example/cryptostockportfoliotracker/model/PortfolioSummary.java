package org.example.cryptostockportfoliotracker.model;

import java.util.List;

public record PortfolioSummary(
        List<PortfolioHolding> holdings,
        double totalInvested,
        double portfolioValue,
        double realizedProfit,
        double unrealizedProfit
) {
    public double totalProfitOrLoss() {
        return realizedProfit + unrealizedProfit;
    }
}
