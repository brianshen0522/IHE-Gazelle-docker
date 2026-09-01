package net.ihe.gazelle.user.management.api.interlay.user;

import java.util.List;

/**
 * Response object for user search queries in Gazelle User Management.
 * <p>
 * This record encapsulates a paginated list of user resources, including offset, limit, and total count for pagination.
 * </p>
 * @param users the list of user resources returned by the search
 * @param offset the offset of the first result in the search
 * @param limit the maximum number of results returned
 * @param count the total number of results available
 */
public record UserSearchResponse(List<UserResource> users, Integer offset, Integer limit, Long count) {
}
