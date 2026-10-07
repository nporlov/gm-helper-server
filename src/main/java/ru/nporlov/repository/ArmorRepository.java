package ru.nporlov.repository;

import ru.nporlov.model.Armor;

import java.util.List;
import java.util.Optional;

public interface ArmorRepository {
    List<Armor> findAll();
    Optional<Armor> findById(Integer id);
    Armor save(Armor armor);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}
