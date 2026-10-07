package ru.nporlov.repository.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.nporlov.model.Armor;
import ru.nporlov.model.ArmorType;
import ru.nporlov.model.SavingThrowName;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonArmorRepositoryTest {
    @TempDir
    Path tempDir;

    Path dataDir;
    JsonArmorRepository repository;
    ObjectMapper mapper;

    private final ArrayList<SavingThrowName> evasionPenalty
            = new ArrayList<>(SavingThrowName.EVASION.ordinal());

    private final ArrayList<SavingThrowName> physicalPenalty
            = new ArrayList<>(SavingThrowName.PHYSICAL.ordinal());

    private final ArrayList<SavingThrowName> mentalPenalty
            = new ArrayList<>(SavingThrowName.MENTAL.ordinal());

    @BeforeEach
    void setUp() {
        dataDir = tempDir.resolve("armor");
        mapper = new ObjectMapper();
        repository = new JsonArmorRepository(dataDir, mapper);
    }

    @Test
    void savedArmorIdNotNull() {
        assertNotNull(repository
                .save(new Armor(ArmorType.LIGHT, false, null))
                .getId()
        );
    }

    @Test
    void save_thenFindById_returnsSameObject() {
        Armor armor = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        repository.save(armor);

        Armor found = repository.findById(armor.getId()).get();
        assertEquals(armor, found);
    }

    @Test
    void save_twoArmors_assignsDifferentIds() {
        Armor first = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        Armor second = new Armor(ArmorType.MEDIUM, false,physicalPenalty);

        repository.save(first);
        repository.save(second);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void save_withExistingId_updates() {
        Armor armor = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        repository.save(armor);
        Integer id = armor.getId();

        Armor updatedArmor = new Armor(id, ArmorType.HEAVY, false,evasionPenalty);
        repository.save(updatedArmor);

        assertEquals(ArmorType.HEAVY, repository.findById(id).get().getType());
    }

    @Test
    void findById_notExisting_returnsEmpty() {
        assertEquals(repository.findById(1), Optional.empty());
    }

    @Test
    void findAll_emptyDir_returnsEmptyList() {
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void findAll_returnAllSaved() {
        Armor first = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        Armor second = new Armor(ArmorType.MEDIUM, false,physicalPenalty);
        Armor third = new Armor(ArmorType.HEAVY, false,mentalPenalty);

        repository.save(first);
        repository.save(second);
        repository.save(third);

        assertEquals(3, repository.findAll().size());
    }

    @Test
    void deleteById_removesSaved() {
        Armor armor = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        repository.save(armor);
        repository.deleteById(armor.getId());

        assertEquals(Optional.empty(), repository.findById(armor.getId()));
    }

    @Test
    void deleteById_notExisting_doesNotThrow() {
        assertDoesNotThrow(() -> repository.deleteById(1));
    }

    @Test
    void existsById_true() {
        Armor armor = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        repository.save(armor);
        assertTrue(repository.existsById(armor.getId()));
    }

    @Test
    void existsById_false() {
        assertFalse(repository.existsById(1));
    }

    @Test
    void save_createsJsonFile() {
        Armor armor = new Armor(ArmorType.LIGHT, false,evasionPenalty);
        repository.save(armor);

        assertTrue(Files.exists(dataDir.resolve(armor.getId() + ".json")));
    }

    @Test
    void save_doesNotLeaveTmpFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "*.tmp")) {
            assertFalse(stream.iterator().hasNext(), "Temporary files left behind");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteById_removesFile() {
        Armor armor = new Armor(ArmorType.LIGHT, false,physicalPenalty);
        repository.save(armor);
        repository.deleteById(armor.getId());

        assertFalse(Files.exists(dataDir.resolve(armor.getId() + ".json")));
    }

    @Test
    void constructor_createsDataDir() {
        Path testDir = tempDir.resolve("test");
        assertFalse(Files.exists(testDir));

        new JsonArmorRepository(testDir, mapper);
        assertTrue(Files.exists(testDir));
        assertTrue(Files.isDirectory(testDir));
    }

    @Test
    void newRepository_onSameDir_continuesIdSequence() {
        Armor first = new Armor(ArmorType.LIGHT, false,physicalPenalty);
        Armor second = new Armor(ArmorType.HEAVY, false,mentalPenalty);

        repository.save(first);
        repository.save(second);

        JsonArmorRepository newRepository = new JsonArmorRepository(dataDir, mapper);
        Armor third = new Armor(ArmorType.MEDIUM, false, evasionPenalty);

        newRepository.save(third);

        assertEquals(3, third.getId());
    }

    @Test
    void newRepository_onEmptyDir_startsFromOne() {
         Armor armor =  new Armor(ArmorType.LIGHT, false,physicalPenalty);
         repository.save(armor);

         assertEquals(1, armor.getId());
    }

    @Test
    void newRepository_ignoresGarbageFiles() {
        Path abcPath = dataDir.resolve("abc.json");
        Path tempPath = dataDir.resolve("12.json.tmp");
        Path correctPath = dataDir.resolve("42.json");
        String content = "{}";
        try {
            Files.writeString(abcPath, content);
            Files.writeString(tempPath, content);
            Files.writeString(correctPath, content);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        Armor armor = new Armor(ArmorType.LIGHT, false,physicalPenalty);
        JsonArmorRepository newRepository = new JsonArmorRepository(dataDir, mapper);
        newRepository.save(armor);

        assertEquals(43, armor.getId());
    }
}