package ru.nporlov.dto;

import ru.nporlov.model.ArmorType;
import ru.nporlov.model.SavingThrowName;

import java.util.List;

public class ArmorDto {
    int id;
    ArmorType type;
    boolean isShield;
    List<SavingThrowName> penalties;
}
