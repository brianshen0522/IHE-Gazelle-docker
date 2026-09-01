package net.ihe.gazelle.user.management.api.interlay.user;

import java.util.List;

/**
 * Response object for user summary search queries in Gazelle User Management.
 * <p>
 * This record encapsulates a paginated list of user summaries, including offset and limit for pagination.
 * </p>
 *
 * @param users the list of user summary resources returned by the search
 * @param offset the offset of the first result in the search
 * @param limit the maximum number of results returned
 */
public record UserSummarySearchResponse(List<UserSummaryResource> users, Integer offset, Integer limit) {
}
