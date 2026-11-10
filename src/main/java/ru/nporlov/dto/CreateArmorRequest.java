package ru.nporlov.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.nporlov.model.ArmorType;
import ru.nporlov.model.SavingThrowName;

import java.util.List;

public class CreateArmorRequest {
    private ArmorType type;
    private Boolean shield;
    private List<SavingThrowName> penalties;

    // EMPTY CONSTRUCTOR
    public CreateArmorRequest() {    }

    @JsonCreator
    public CreateArmorRequest(
            @JsonProperty("type") ArmorType type,
            @JsonProperty("shield") boolean shield,
            @JsonProperty("penalties") List<SavingThrowName> penalties
    ) {
        this.type = type;
        this.shield = shield;
        this.penalties = penalties;
    }

    public ArmorType getType() {
        return this.type;
    }
    public Boolean isShield() {
        return this.shield;
    }
    public List<SavingThrowName> getPenalties() {
        return this.penalties;
    }
}
