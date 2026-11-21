package util.paging;

public record Pageable(int pageNumber, int pageSize) {

    public Pageable {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if  (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
    }

    public int offset() {
        return pageNumber * pageSize;
    }
}
