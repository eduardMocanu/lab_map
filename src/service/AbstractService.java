package service;

import exceptions.EntityNotFoundException;
import model.Entity;
import repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base class for services that add and remove entities, using the Template Method pattern.
 * The steps of {@link #add(Entity)} and {@link #remove(Object)} are fixed here;
 * subclasses only fill in the hooks {@link #checkBeforeAdd(Entity)} and {@link #beforeRemove(Object)}.
 * <p>
 * The service depends only on the repository interface, never on a concrete implementation.
 * The entity's own data is validated by the repository when it is saved;
 * the service checks the rules that need other data (uniqueness, related entities).
 *
 * @param <ID> the type of the entity id
 * @param <E>  the type of the entity
 * @param <R>  the type of the repository that stores the entities
 */
public abstract class AbstractService<ID, E extends Entity<ID>, R extends Repository<ID, E>> {

    /** The repository that stores the entities managed by this service. */
    protected final R repository;

    /**
     * @param repository the repository that stores the entities
     */
    protected AbstractService(R repository) {
        this.repository = repository;
    }

    /**
     * Finds the entity with the given id.
     *
     * @param id the id of the entity
     * @return the entity, or an empty Optional if none has this id
     * @throws IllegalArgumentException if {@code id} is null
     */
    public Optional<E> findOne(ID id) {
        return repository.findOne(id);
    }

    /**
     * Returns all entities managed by this service.
     *
     * @return a list of all entities; empty if there are none
     */
    public List<E> getAll() {
        return repository.findAll().stream().toList();
    }

    /**
     * Hook called by {@link #add(Entity)} before the entity is saved.
     * Subclasses check here the rules that depend on other data (uniqueness, related entities existing),
     * and throw an exception if the entity cannot be added.
     * The entity has not been validated yet, so implementations must accept null fields.
     *
     * @param entity the entity about to be added
     */
    protected abstract void checkBeforeAdd(E entity);

    /**
     * Hook called by {@link #remove(Object)} after the entity is known to exist and before it is deleted.
     * Subclasses clean up here any data that depends on the entity. The default does nothing.
     *
     * @param id the id of the entity about to be removed
     */
    protected void beforeRemove(ID id) {
        // nothing to clean up by default
    }

    /**
     * Adds an entity: checks the business rules, then saves it (the repository validates it).
     *
     * @param entity the entity to add
     * @return the saved entity
     * @throws exceptions.ValidationException      if the entity is not valid
     * @throws exceptions.DuplicateEntityException if the entity conflicts with existing data
     * @throws EntityNotFoundException             if an entity it refers to does not exist
     * @throws exceptions.RepositoryException      if the entity cannot be persisted
     */
    public final E add(E entity) {
        checkBeforeAdd(entity);
        return repository.save(entity);
    }

    /**
     * Removes an entity: checks it exists, lets the subclass clean up related data, then deletes it.
     *
     * @param id the id of the entity to remove
     * @return the removed entity
     * @throws EntityNotFoundException        if no entity has this id
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public final E remove(ID id) {
        Optional<E> found = repository.findOne(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Entity with id " + id + " does not exist.");
        }
        beforeRemove(id);
        repository.delete(id);
        return found.get();
    }
}
