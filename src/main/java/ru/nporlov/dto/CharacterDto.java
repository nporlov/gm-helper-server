package ru.nporlov.dto;

import ru.nporlov.model.CharacterClass;

import java.util.List;

public class CharacterDto {
    int id;
    String name;
    ArmorDto armor;
    WeaponDto weapon;
    CharacterClass characterClass;
    int level;
    int experience;
    boolean isAlive;
    List<AbilityDto> abilities;
    List<SavingThrowDto> savingThrows;
}
