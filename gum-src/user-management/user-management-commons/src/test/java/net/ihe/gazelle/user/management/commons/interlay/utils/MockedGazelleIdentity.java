package net.ihe.gazelle.user.management.commons.interlay.utils;

import net.ihe.gazelle.security.business.GazelleIdentity;

import java.security.Principal;
import java.util.Set;

public class MockedGazelleIdentity implements GazelleIdentity {

    private Set<String> groups;
    private String id;
    private String orgaId;

    public MockedGazelleIdentity(Set<String> groups) {
        this.groups = groups;
        this.id="";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Set<String> getGroups() {
        return groups;
    }

    @Override
    public String getOrganizationGroup() {
        return null;
    }

    @Override
    public String getOrganizationId() {
        return orgaId;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean hasGroup(String groupName) {
        return groups.contains(groupName);
    }

    public MockedGazelleIdentity setGroups(Set<String> groups) {
        this.groups = groups;
        return this;
    }

    public MockedGazelleIdentity setIdentityId(String id) {
        this.id = id;
        return this;
    }

    public MockedGazelleIdentity setOrganizationId(String orgaId) {
        this.orgaId = orgaId;
        return this;
    }
}
