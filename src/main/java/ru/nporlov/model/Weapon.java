package ru.nporlov.model;

import java.util.List;

public class Weapon {
    private int id;
    private WeaponType  type;
    private List<Integer> ammunition;
    private int attackBonus;
    private Roll attackRoll;
}
