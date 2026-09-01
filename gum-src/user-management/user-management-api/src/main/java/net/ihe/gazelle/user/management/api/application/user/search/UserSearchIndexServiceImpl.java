package net.ihe.gazelle.user.management.api.application.user.search;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.IndexedField;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link IndexService} for user search. It contains the list of indexed fields for user search.
 */
public class UserSearchIndexServiceImpl implements IndexService {

    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String EMAIL = "email";
    public static final String GROUP = "group";
    public static final String ACTIVATED = "activated";
    public static final String DELEGATED = "delegated";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String SEARCH = "search";
    public static final String LAST_LOGIN_TIMESTAMP = "lastLoginTimestamp";


    private static final Map<String, IndexedField> INDEXES = new LinkedHashMap<>();

    static {
        INDEXES.put(FIRSTNAME, new IndexedField(FIRSTNAME, IndexedField.Type.STRING));
        INDEXES.put(LASTNAME, new IndexedField(LASTNAME, IndexedField.Type.STRING));
        INDEXES.put(EMAIL, new IndexedField(EMAIL, IndexedField.Type.STRING));
        INDEXES.put(GROUP, new IndexedField(GROUP, IndexedField.Type.STRING));
        INDEXES.put(ACTIVATED, new IndexedField(ACTIVATED, IndexedField.Type.BOOLEAN));
        INDEXES.put(DELEGATED, new IndexedField(DELEGATED, IndexedField.Type.BOOLEAN));
        INDEXES.put(ORGANIZATION_NAME, new IndexedField(ORGANIZATION_NAME, IndexedField.Type.STRING));
        INDEXES.put(ORGANIZATION_ID, new IndexedField(ORGANIZATION_ID, IndexedField.Type.STRING));
        INDEXES.put(SEARCH, new IndexedField(SEARCH, IndexedField.Type.STRING));
        INDEXES.put(LAST_LOGIN_TIMESTAMP, new IndexedField(LAST_LOGIN_TIMESTAMP, IndexedField.Type.DATE));
    }

    @Override
    public Map<String, IndexedField> getIndexes() {
        return new LinkedHashMap<>(INDEXES);
    }
}
