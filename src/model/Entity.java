package model;

import java.util.Objects;

/**
 * Base class for all domain entities.
 * Every entity has an identifier, whose type is chosen by the subclass.
 * Two entities are equal if they are of the same class and have the same id.
 *
 * @param <ID> the type of the identifier
 */
public class Entity<ID> {
    private ID id;

    /**
     * Creates an entity without an id; the id is set later with {@link #setId(Object)}.
     */
    public Entity() {
    }

    /**
     * @return the id of the entity, or {@code null} if it has not been assigned yet
     */
    public ID getId() {
        return id;
    }

    /**
     * @param id the new id of the entity
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Compares two entities by class and id.
     * Returns false for null and for objects of another class (no direct cast).
     *
     * @param o the object to compare with
     * @return true if {@code o} is an entity of the same class with the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * @return a hash code based on the id, consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
