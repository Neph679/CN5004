package org.example.cryptostockportfoliotracker.model;

    public class Transaction {

        private String asset;
        private String type;
        private double quantity;
        private double price;

        public Transaction(String asset, String type, double quantity, double price) {
            this.asset = asset;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
        }

        public String getAsset() { return asset; }
        public String getType() { return type; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }

        public double getTotal() {
            return quantity * price;
        }
    }