package ru.nporlov.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Armor {
    private Integer id = null;
    private ArmorType type;
    @JsonProperty ("is-shield")
    private boolean isShield;
    private List<SavingThrowName> penalties;

    public Armor(ArmorType type, boolean isShield, List<SavingThrowName> penalties) {
        this.type = type;
        this.isShield = isShield;
        this.penalties = penalties;
    }

    @JsonCreator
    public Armor(
            @JsonProperty("id") Integer id,
            @JsonProperty("type") ArmorType type,
            @JsonProperty("is-shield") boolean isShield,
            @JsonProperty("penalties")  List<SavingThrowName> penalties) {
        this.id = id;
        this.type = type;
        this.isShield = isShield;
        this.penalties = penalties;
    }
    public Armor() {}

    public Integer getId () {
        return this.id;
    }
    public ArmorType getType () { return this.type; }
    public boolean isShield () { return this.isShield; }
    public List<SavingThrowName> getPenalties () { return this.penalties; }


    public void setId (Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Armor armor = (Armor) o;
        return Objects.equals(id, armor.id)
                && Objects.equals(type, armor.type)
                && Objects.equals(isShield, armor.isShield)
                && Objects.equals(penalties, armor.penalties);
    }

    @Override
    public int hashCode () {
        return Objects.hash(id, type, isShield, penalties);
    }
}
