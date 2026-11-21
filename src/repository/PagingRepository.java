package repository;

import model.Entity;
import util.paging.Page;
import util.paging.Pageable;

/** A repository that can return its entities one page at a time. */

public interface PagingRepository <ID, E extends Entity<ID>> extends Repository<ID, E>{
    /**
     * Returns one page of entities, ordered by id.
     *
     * @param pageable which page to return and how many entities per page
     * @return the entities on that page and the total number of entities; an empty page after the last one
     * @throws IllegalArgumentException       if {@code pageable} is null
     * @throws exceptions.RepositoryException if the storage cannot be read
     */
    Page<E> findAllOnPage(Pageable pageable);
}
