package ru.nporlov.model;

import java.util.List;

public class Roll {
    private String notation;
    private List<Die> diceRolls;
    private int modifier;

    // GETTERS
    public String getNotation() {
        return notation;
    }

    public List<Die> getDiceRolls() {
        return diceRolls;
    }

    public int getModifier() {
        return modifier;
    }
}
