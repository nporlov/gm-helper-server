package ru.nporlov.repository;

import ru.nporlov.model.Armor;

import java.util.List;
import java.util.Optional;

public interface ArmorRepository {
    List<Armor> findAll();
    Optional<Armor> findById(int id);
    Armor save(Armor armor);
    void deleteById(int id);
    boolean existsById(int id);
}
