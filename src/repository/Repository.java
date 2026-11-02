package repository;

import model.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic CRUD repository for entities.
 * Implementations decide where the entities are stored (memory, file, database);
 * the layers above depend only on this contract, and the concrete implementation
 * is chosen by {@link repository.factory.RepositoryFactory} from the configuration.
 *
 * @param <ID> the type of the entity id
 * @param <E>  the type of the entity, which must have an id of type {@code ID}
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds the entity with the given id.
     *
     * @param id the id of the entity to find
     * @return the entity, or an empty Optional if none has this id
     * @throws IllegalArgumentException if {@code id} is null
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all stored entities.
     *
     * @return all entities; empty if there are none
     */
    List<E> findAll();

    /**
     * Finds all entities that pass the filter
     * @param filter the predicate on which we base our filtering
     * @return all the elements of type E that pass the filtering
     */

    default List<E> findAll(Predicate<E> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null.");
        }
        return findAll().stream()
                .filter(filter)
                .toList();
    }

    /**
     * Validates and stores a new entity.
     *
     * @param entity the entity to store
     * @return the stored entity (with its id set)
     * @throws IllegalArgumentException            if {@code entity} is null
     * @throws exceptions.ValidationException      if the entity is not valid
     * @throws exceptions.DuplicateEntityException if an entity with the same id already exists
     * @throws exceptions.RepositoryException      if the entity cannot be persisted
     */
    E save(E entity);

    /**
     * Removes the entity with the given id.
     *
     * @param id the id of the entity to remove
     * @return the removed entity, or an empty Optional if none had this id
     * @throws IllegalArgumentException       if {@code id} is null
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    Optional<E> delete(ID id);

    /**
     * Validates an entity and replaces the stored entity that has the same id.
     *
     * @param entity the entity with new data
     * @return the old entity, or an empty Optional if none had this id
     * @throws IllegalArgumentException       if {@code entity} is null
     * @throws exceptions.ValidationException if the entity is not valid
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    Optional<E> update(E entity);
}
