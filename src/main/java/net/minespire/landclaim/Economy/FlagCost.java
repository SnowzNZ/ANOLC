package net.minespire.landclaim.Economy;

public class FlagCost {
    private String flag;
    private double cost;

    public FlagCost(final String flag, final double cost) {
        this.flag = flag.toLowerCase();
        this.cost = cost;
    }

    public String getFlag() {
        return this.flag;
    }

    public double getCost() {
        return this.cost;
    }
}

