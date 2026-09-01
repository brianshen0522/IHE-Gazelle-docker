    package net.ihe.gazelle.user.management.api.application.user.lookup;

/**
 * Sorting order for user lookup queries.
 */
public enum SortOrder {
    /** Ascending sort order. */
    ASC,
    /** Descending sort order. */
    DESC;

    SortOrder() {
    }

    /**
     * Returns the sort order for the given string, defaulting to ASC.
     *
     * @param sortOrder string value of the sort order
     * @return the matching sort order or ASC when not DESC
     */
    public static SortOrder getByString(String sortOrder) {
        if (DESC.name().equals(sortOrder)) {
            return DESC;
        } else {
            return ASC;
        }
    }
}
