package util.paging;

import java.util.List;


/**
 * One page of results: the elements on it and the total number of elements in all pages.
 *
 * @param elementsOnPage        the elements on this page (empty after the last page)
 * @param totalNumberOfElements the number of elements in all pages together
 * @param <E>                   the type of the elements
 */
public record Page<E>(List<E> elementsOnPage, int totalNumberOfElements) {


    /**
     * Computes how many pages the elements fill.
     *
     * @param pageSize the number of elements per page
     * @return the number of pages, at least 1 (so an empty result is shown as "Page 1 of 1")
     * @throws IllegalArgumentException if {@code pageSize <= 0}
     */
    public int totalPages(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive.");
        }
        return Math.max(1, (totalNumberOfElements + pageSize - 1) / pageSize);
    }

}
