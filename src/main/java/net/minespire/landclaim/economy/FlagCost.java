package net.minespire.landclaim.economy;

public class FlagCost {
    private final String flag;
    private final double cost;

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

