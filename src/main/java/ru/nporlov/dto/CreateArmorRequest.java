package ru.nporlov.dto;

import ru.nporlov.model.ArmorType;
import ru.nporlov.model.SavingThrowName;

import java.util.List;

public class CreateArmorRequest {
    private ArmorType type;
    private Boolean isShield;
    private List<SavingThrowName> penalties;

    public ArmorType getType() {
        return this.type;
    }
    public Boolean isShield() {
        return this.isShield;
    }
    public List<SavingThrowName> getPenalties() {
        return this.penalties;
    }
}
