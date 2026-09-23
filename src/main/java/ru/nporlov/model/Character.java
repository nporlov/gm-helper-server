package ru.nporlov.model;

import java.util.List;

public class Character {
    private int id;
    private String name;
    private CharacterClass characterClass;
    private int level;
    private int experience;
    private boolean isAlive;
    private List<Ability> abilities;
    private List<SavingThrow>  savingThrows;
    private Armor armor;
}
