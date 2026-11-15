package com.fooddelivery.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileDataStore {

    private static final Logger log = LoggerFactory.getLogger(FileDataStore.class);
    private static final DateTimeFormatter BACKUP_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final int MAX_BACKUPS = 20;

    private final ObjectMapper mapper;
    private final DataValidator validator;
    private final Path dataFile;
    private final Path backupFile;
    private final Path backupDirectory;

    public FileDataStore(FileStorageProperties properties, ObjectMapper objectMapper, DataValidator validator) {
        this.mapper = objectMapper.copy();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.validator = validator;
        Path directory = Path.of(properties.getDataDirectory()).toAbsolutePath().normalize();
        this.dataFile = directory.resolve(properties.getFileName());
        this.backupFile = directory.resolve(properties.getFileName() + ".bak");
        this.backupDirectory = directory.resolve("backups");
    }

    public Path getDataFile() {
        return dataFile;
    }

    public ApplicationData loadOrCreate() {
        createDirectories();
        if (!Files.exists(dataFile) || isBlank(dataFile)) {
            log.info("Creating a new JSON data file at {}", dataFile);
            ApplicationData empty = ApplicationData.empty();
            save(empty);
            return empty;
        }
        ApplicationData loaded = tryRead(dataFile);
        if (loaded != null && passes(loaded)) {
            log.info("Loaded JSON data from {}", dataFile);
            return loaded;
        }
        log.error("Primary JSON file is unreadable or failed validation: {}", dataFile);
        ApplicationData recovered = recover();
        if (recovered == null) {
            throw new DataStoreException("JSON data file is corrupted and no valid backup is available: " + dataFile
                    + ". Writes are blocked until the file is repaired.");
        }
        log.warn("Restored JSON data from a backup into {}", dataFile);
        save(recovered);
        return recovered;
    }

    public ApplicationData readExisting() {
        ApplicationData loaded = tryRead(dataFile);
        if (loaded == null || !passes(loaded)) {
            throw new DataStoreException("The JSON data file cannot be read safely: " + dataFile);
        }
        return loaded;
    }

    public synchronized void save(ApplicationData data) {
        data.ensureCollections();
        data.alignSequences();
        validator.validate(data);
        createDirectories();
        if (Files.exists(dataFile)) {
            ApplicationData current = tryRead(dataFile);
            if (current != null && current.hasRecords() && !data.hasRecords()) {
                throw new DataStoreException("Refusing to replace populated JSON data with an empty store");
            }
        }
        Path tempFile = dataFile.resolveSibling(dataFile.getFileName().toString() + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                mapper.writeValue(writer, data);
            }
            ApplicationData reread = tryRead(tempFile);
            if (reread == null || !passes(reread)) {
                throw new DataStoreException("Temporary JSON file failed validation and was not saved");
            }
            if (Files.exists(dataFile) && !isBlank(dataFile)) {
                Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
                String stamp = LocalDateTime.now().format(BACKUP_STAMP);
                Path historic = backupDirectory.resolve("food-delivery-data-" + stamp + ".json");
                Files.copy(dataFile, historic, StandardCopyOption.REPLACE_EXISTING);
                pruneBackups();
            }
            moveIntoPlace(tempFile);
            log.debug("Saved JSON data to {}", dataFile);
        } catch (DataStoreException ex) {
            deleteQuietly(tempFile);
            throw ex;
        } catch (IOException ex) {
            deleteQuietly(tempFile);
            throw new DataStoreException("Could not save the JSON data file: " + dataFile, ex);
        }
    }

    private void moveIntoPlace(Path tempFile) throws IOException {
        try {
            Files.move(tempFile, dataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException | java.nio.file.AccessDeniedException ex) {
            log.debug("Atomic move was not available ({}). Using a standard replace.", ex.getClass().getSimpleName());
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ApplicationData recover() {
        ApplicationData backup = tryRead(backupFile);
        if (backup != null && passes(backup)) {
            return backup;
        }
        if (!Files.isDirectory(backupDirectory)) {
            return null;
        }
        try (Stream<Path> files = Files.list(backupDirectory)) {
            List<Path> candidates = files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(this::modifiedAt).reversed())
                    .toList();
            for (Path candidate : candidates) {
                ApplicationData historic = tryRead(candidate);
                if (historic != null && passes(historic)) {
                    return historic;
                }
            }
        } catch (IOException ex) {
            log.warn("Could not list backups in {}: {}", backupDirectory, ex.getMessage());
        }
        return null;
    }

    private ApplicationData tryRead(Path path) {
        try {
            if (!Files.exists(path) || Files.size(path) == 0) {
                return null;
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ApplicationData data = mapper.readValue(reader, ApplicationData.class);
                if (data == null) {
                    return null;
                }
                data.ensureCollections();
                data.alignSequences();
                return data;
            }
        } catch (Exception ex) {
            log.warn("Could not parse {}: {}", path, ex.getMessage());
            return null;
        }
    }

    private boolean passes(ApplicationData data) {
        try {
            validator.validate(data);
            return true;
        } catch (RuntimeException ex) {
            log.warn("JSON data failed validation: {}", ex.getMessage());
            return false;
        }
    }

    private boolean isBlank(Path path) {
        try {
            if (!Files.exists(path) || Files.size(path) == 0) {
                return true;
            }
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            return content.isEmpty();
        } catch (IOException ex) {
            return false;
        }
    }

    private void createDirectories() {
        try {
            Files.createDirectories(dataFile.getParent());
            Files.createDirectories(backupDirectory);
        } catch (IOException ex) {
            throw new DataStoreException("Could not create the data directory " + dataFile.getParent(), ex);
        }
    }

    private void pruneBackups() throws IOException {
        try (Stream<Path> files = Files.list(backupDirectory)) {
            List<Path> backups = files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(this::modifiedAt).reversed())
                    .toList();
            for (int i = MAX_BACKUPS; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        }
    }

    private java.nio.file.attribute.FileTime modifiedAt(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException ex) {
            return java.nio.file.attribute.FileTime.fromMillis(0);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            log.warn("Could not delete temporary file {}", path);
        }
    }
}
