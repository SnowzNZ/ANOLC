package net.minespire.landclaim.economy;

public record FlagCost(String flag, double cost) {
    public FlagCost(final String flag, final double cost) {
        this.flag = flag.toLowerCase();
        this.cost = cost;
    }
}

