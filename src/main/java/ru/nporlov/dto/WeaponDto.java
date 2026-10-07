package ru.nporlov.dto;

import ru.nporlov.model.WeaponType;

import java.util.List;

public class WeaponDto {
    int id;
    WeaponType type;
    List<Integer> ammunition;
    int attackBonus;
}
