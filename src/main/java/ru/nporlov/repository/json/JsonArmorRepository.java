package ru.nporlov.repository.json;

import ru.nporlov.model.Armor;
import ru.nporlov.repository.ArmorRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonArmorRepository implements ArmorRepository {
    Path dataDir;
    ObjectMapper mapper;
    int idSequence;

    // CONSTRUCTOR
    public JsonArmorRepository(Path dataDir, ObjectMapper mapper) {
        this.dataDir = dataDir;
        this.mapper = mapper;
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create directory: " + dataDir, e);
        }
        this.idSequence = findMaxId(dataDir);
    }

    public List<Armor> findAll() {
        List<Armor> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "[0-9]*.json")) {
            for (Path file : stream) {
                result.add(mapper.readValue(file.toFile(), Armor.class));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    public Optional<Armor> findById(Integer id) {
        Path file = dataDir.resolve(id + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        return Optional.of(mapper.readValue(file.toFile(), Armor.class));
    }

    public Armor save(Armor armor) {
        try {
            if (null == armor.getId()) {
                armor.setId(++idSequence);
            }
            Path target = dataDir.resolve(armor.getId() + ".json");
            Path temp = dataDir.resolve(armor.getId() + ".json.tmp");

            mapper.writeValue(temp.toFile(), armor);
            Files.move(temp, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            return armor;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void deleteById(Integer id) {
        try {
            Path file = dataDir.resolve(id + ".json");
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean existsById(Integer id) {
        return Files.exists(dataDir.resolve(id + ".json"));
    }

    private static int findMaxId(Path dataDir) {
        int maxId = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "[0-9]*.json")) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                String idPart = name.substring(0, name.length() - ".json".length());
                try {
                    maxId = Math.max(maxId, Integer.parseInt(idPart));
                }
                catch (NumberFormatException e) {
                    throw new NumberFormatException("Wrong name format for file: " + name);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot scan directory: " + dataDir, e);
        }
        return maxId;
    }
}
