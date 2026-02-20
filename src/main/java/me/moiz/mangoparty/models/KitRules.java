package me.moiz.mangoparty.models;

public class KitRules {
    private boolean naturalRegen = true;
    private boolean allowBlockBreak = false;
    private boolean allowBlockPlace = false;
    private double damageMultiplier = 1.0;
    private boolean instantTNT = false;

    public KitRules() {}

    public boolean isNaturalRegen() {
        return naturalRegen;
    }

    public void setNaturalRegen(boolean naturalRegen) {
        this.naturalRegen = naturalRegen;
    }

    public boolean isAllowBlockBreak() {
        return allowBlockBreak;
    }

    public void setAllowBlockBreak(boolean allowBlockBreak) {
        this.allowBlockBreak = allowBlockBreak;
    }

    public boolean isAllowBlockPlace() {
        return allowBlockPlace;
    }

    public void setAllowBlockPlace(boolean allowBlockPlace) {
        this.allowBlockPlace = allowBlockPlace;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public boolean isInstantTNT() {
        return instantTNT;
    }

    public void setInstantTNT(boolean instantTNT) {
        this.instantTNT = instantTNT;
    }
}
