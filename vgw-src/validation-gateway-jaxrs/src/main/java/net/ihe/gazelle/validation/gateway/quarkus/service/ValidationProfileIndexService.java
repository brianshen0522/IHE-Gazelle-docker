package net.ihe.gazelle.validation.gateway.quarkus.service;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.IndexedField;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ValidationProfileIndexService implements IndexService {

    private final Map<String, IndexedField> indexes = buildIndexes();

    @Override
    public Map<String, IndexedField> getIndexes() {
        return indexes;
    }

    @Override
    public Set<IndexedField> getIndexedFields() {
        return Set.copyOf(indexes.values());
    }

    @Override
    public IndexedField getIndexedField(String indexName) {
        return indexes.get(indexName);
    }

    @Override
    public boolean isIndexedField(String indexName) {
        return indexes.containsKey(indexName);
    }

    private Map<String, IndexedField> buildIndexes() {
        Map<String, IndexedField> map = new LinkedHashMap<>();
        map.put("validationService", new IndexedField("validationService", IndexedField.Type.STRING));
        map.put("profileID", new IndexedField("profileID", IndexedField.Type.STRING));
        map.put("profileName", new IndexedField("profileName", IndexedField.Type.STRING));
        map.put("version", new IndexedField("version", IndexedField.Type.STRING));
        map.put("domain", new IndexedField("domain", IndexedField.Type.STRING));
        map.put("coveredItems", new IndexedField("coveredItems", IndexedField.Type.STRING));
        map.put("standards", new IndexedField("standards", IndexedField.Type.STRING));
        map.put("tags", new IndexedField("tags", IndexedField.Type.STRING));
        return Map.copyOf(map);
    }
}
