package ru.nporlov.model;

import java.util.List;

public class Armor {
    private Integer id = null;
    private ArmorType type;
    private boolean isShield;
    private List<SavingThrowName> penalties;

    public Armor(ArmorType type, boolean isShield, List<SavingThrowName> penalties) {
        this.type = type;
        this.isShield = isShield;
        this.penalties = penalties;
    }

    public Integer getId () {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }
}
