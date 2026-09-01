package net.ihe.gazelle.validation.gateway.technical.cache;

import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.util.List;

public interface ProfileFetchCapable {

   ProfileFetchResponse fetchProfiles(String ifNoneMatch);

   record ProfileFetchResponse(int status, String etag, List<ValidationProfile> profiles) {
   }
}
