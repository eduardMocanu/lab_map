package repository.memory;

import exceptions.DuplicateEntityException;
import model.Entity;
import repository.Repository;
import validators.Validator;

import java.util.*;

/**
 * Repository that keeps entities in a map in memory.
 * Every entity is validated (with the injected {@link Validator}) before it is stored.
 * Data is lost when the application stops; {@link repository.file.AbstractFileRepository}
 * extends this class to mirror the map in a text file.
 *
 * @param <ID> the type of the entity id
 * @param <E>  the type of the entity
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {

    /** The stored entities, indexed by id. */
    protected final Map<ID, E> entities = new HashMap<>();

    /** The strategy used to validate every entity before it is stored. */
    protected final Validator<E> validator;

    /**
     * @param validator the validator applied to every entity before it is stored
     * @throws IllegalArgumentException if {@code validator} is null
     */
    public InMemoryRepository(Validator<E> validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null.");
        }
        this.validator = validator;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        return Optional.ofNullable(entities.get(id));
    }

    /**
     * {@inheritDoc}
     * The result is a copy, so it can be iterated safely while the repository changes.
     */
    @Override
    public List<E> findAll() {
        return new ArrayList<>(entities.values());
    }

    /** {@inheritDoc} */
    @Override
    public E save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }
        validator.validate(entity);
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity id cannot be null.");
        }
        if (entities.containsKey(entity.getId())) {
            throw new DuplicateEntityException("Entity with id " + entity.getId() + " already exists.");
        }
        entities.put(entity.getId(), entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        return Optional.ofNullable(entities.remove(id));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }
        validator.validate(entity);
        if (entity.getId() == null || !entities.containsKey(entity.getId())) {
            return Optional.empty();
        }
        return Optional.ofNullable(entities.put(entity.getId(), entity));
    }
}
