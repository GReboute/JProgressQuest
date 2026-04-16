package com.JProgressQuest.util;

/**
 * Classe utilitaire autonome pour simuler l'évolution du prix des équipements.
 */
public class EquipmentPriceCalculator {

    public static void main(String[] args) {
        System.out.println("=== Évolution du prix de l'équipement dans JProgressQuest ===");
        System.out.printf("%-10s | %-15s | %-15s| %-6s%n", "Niveau", "Prix (Or)", "Prix avec Exp", "Old>New");
        System.out.println("-----------|----------------|----------------|----------------|------");

        for (int level = 1; level <= 100; level++) {
            int price = calculateEquipmentPrice(level);
            int priceExp = calculateEquipmentPriceExp(level);
            double itemprice = level*Math.pow(10.0, (1+level/100.0));
            System.out.printf("%-10d | %-15d | %-15d | %-15f | %b%n", level, price, priceExp, itemprice, price>priceExp);
        }
    }

    /**
     * Calcule le prix de l'équipement selon la formule définie dans GameService.java.
     */
    public static int calculateEquipmentPrice(int level) {
        return 5 * (level * level) + (10 * level) + 20;
    }

    public static int calculateEquipmentPriceExp(int level) {
        return 5 * (level * level) + ((int) Math.ceil(Math.pow(1.21, level))) + 20;
    }
}
