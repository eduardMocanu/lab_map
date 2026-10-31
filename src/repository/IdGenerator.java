package repository;

import model.Entity;

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
    public static Long nextId(Iterable<? extends Entity<Long>> entities) {
        long max = 0;
        for (Entity<Long> entity : entities) {
            Long id = entity.getId();
            if (id != null && id > max) {
                max = id;
            }
        }
        return max + 1;
    }
}
