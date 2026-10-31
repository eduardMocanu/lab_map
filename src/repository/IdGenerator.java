package repository;

import model.Entity;

import java.util.List;

/**
 * Helper for repositories that generate numeric ids themselves (memory and file storage).
 * A database generates the ids on its own, so it does not need this class.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * Computes the next free id from the entities that are already stored.
     *
     * @param entities the stored entities
     * @return the largest existing id plus 1, or 1 if there are no entities
     */
    public static Long nextId(List<? extends Entity<Long>> entities) {
        return entities.stream().mapToLong(Entity::getId).max().orElse(0L);
    }
}
