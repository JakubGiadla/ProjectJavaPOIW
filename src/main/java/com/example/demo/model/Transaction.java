package com.example.demo.model;

public record Transaction(User from, User to, double amount) {
    @Override
    public String toString() {
        return String.format("%s -> %s: %.2f zł", from, to, amount);
    }
}