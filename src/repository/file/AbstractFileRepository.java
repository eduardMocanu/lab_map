package repository.file;

import exceptions.DuplicateEntityException;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import model.Entity;
import repository.Repository;
import repository.memory.InMemoryRepository;
import validators.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Repository that keeps entities in memory (inherited from {@link InMemoryRepository})
 * and mirrors them in a UTF-8 text file, one entity per line.
 * <p>
 * The file is loaded when the repository is created and rewritten after every successful
 * save, delete or update. Reading and writing are implemented only here; subclasses only
 * convert one line to an entity and back (Template Method pattern).
 * <p>
 * Missing file: the repository starts empty and the file is created at the first save.
 * Corrupt line: the line is reported (file, line number and reason) on the error stream
 * and skipped, so one bad line does not make the rest of the data unavailable.
 *
 * @param <ID> the type of the entity id
 * @param <E>  the type of the entity
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> implements Repository<ID, E> {

    /** Separator between the fields of a line. */
    protected static final String SEPARATOR = ";";

    private final Path filePath;
    private final List<String> loadErrors = new ArrayList<>();

    /**
     * Creates the repository and loads all entities from the file.
     * Note: this constructor calls {@link #extractEntity(String)}; implementations of that method
     * must not use fields of the subclass, because they are not initialized yet at this point.
     *
     * @param validator the validator applied to every entity, including the loaded ones
     * @param fileName  the path of the file, relative to the project folder (for example data/users.txt)
     * @throws IllegalArgumentException if the validator or the file name is null or blank
     * @throws RepositoryException      if the file exists but cannot be read
     */
    protected AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }
        this.filePath = Path.of(fileName);
        loadData();
    }

    /**
     * Converts one line of the file into an entity.
     *
     * @param line a non-blank line from the file
     * @return the entity described by the line
     * @throws RepositoryException if the line does not have the expected format
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into one line of the file, without a line separator.
     *
     * @param entity the entity
     * @return the line that describes the entity
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Splits a line into exactly the expected number of fields.
     * Empty fields at the end of the line are kept (split with limit -1).
     *
     * @param line           the line to split
     * @param expectedFields the number of fields the line must have
     * @return the fields of the line
     * @throws RepositoryException if the line does not have exactly {@code expectedFields} fields
     */
    protected String[] splitLine(String line, int expectedFields) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != expectedFields) {
            throw new RepositoryException("expected " + expectedFields + " fields separated by '"
                    + SEPARATOR + "', found " + parts.length);
        }
        return parts;
    }

    /**
     * @return the problems found while loading the file (one message per skipped line); empty if none
     */
    public List<String> getLoadErrors() {
        return new ArrayList<>(loadErrors);
    }

    /**
     * Reads the file line by line and stores every valid entity.
     * Entities are stored with {@code super.save}, which validates them without rewriting the file.
     * Invalid lines are reported and skipped.
     *
     * @throws RepositoryException if the file exists but cannot be read
     */
    /**
     * Reads the file and stores every valid entity.
     * Lines are read with {@link Files#lines(Path, java.nio.charset.Charset)} and kept in a list,
     * so each invalid line can be reported with its number; invalid lines are skipped.
     *
     * @throws RepositoryException if the file exists but cannot be read
     */
    private void loadData() {
        if (!Files.exists(filePath)) {
            return;
        }
        List<String> lines;
        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
            lines = stream.toList();
        } catch (IOException | UncheckedIOException e) {
            throw new RepositoryException("Could not read file " + filePath + ".", e);
        }

        IntStream.range(0, lines.size())
                .filter(i -> !lines.get(i).isBlank())
                .forEach(i -> loadLine(lines.get(i), i + 1));
    }

    /**
     * Converts one line into an entity and stores it with {@code super.save}, which validates it
     * without rewriting the file. An invalid line is reported (file, line number, reason) and skipped.
     *
     * @param line       the line to load
     * @param lineNumber the number of the line in the file, starting from 1
     */
    private void loadLine(String line, int lineNumber) {
        try {
            super.save(extractEntity(line));
        } catch (RepositoryException | ValidationException | DuplicateEntityException e) {
            String message = filePath + ", line " + lineNumber + " skipped: "
                    + e.getMessage().replace('\n', ' ');
            loadErrors.add(message);
            System.err.println("Warning: " + message);
        }
    }

    /**
     * Rewrites the whole file with the current content of the repository.
     * The lines are built with a stream over the entities and written with {@link Files#write}.
     * The parent folder is created if it does not exist.
     *
     * @throws RepositoryException if the folder or the file cannot be written
     */
    protected void writeToFile() {
        List<String> lines = entities.values().stream()
                .map(this::createEntityAsString)
                .collect(Collectors.toList());
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Could not write file " + filePath + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     * The file is rewritten after the entity is added; if writing fails, the entity is removed again,
     * so memory and file stay consistent.
     */
    @Override
    public E save(E entity) {
        E saved = super.save(entity);
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.remove(saved.getId());
            throw e;
        }
        return saved;
    }

    /**
     * {@inheritDoc}
     * The file is rewritten only if an entity was actually removed; if writing fails,
     * the entity is put back, so memory and file stay consistent.
     */
    @Override
    public Optional<E> delete(ID id) {
        Optional<E> removed = super.delete(id);
        if (removed.isPresent()) {
            try {
                writeToFile();
            } catch (RepositoryException e) {
                entities.put(id, removed.get());
                throw e;
            }
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     * The file is rewritten only if an entity was actually replaced; if writing fails,
     * the old entity is put back, so memory and file stay consistent.
     */
    @Override
    public Optional<E> update(E entity) {
        Optional<E> old = super.update(entity);
        if (old.isPresent()) {
            try {
                writeToFile();
            } catch (RepositoryException e) {
                entities.put(entity.getId(), old.get());
                throw e;
            }
        }
        return old;
    }
}
