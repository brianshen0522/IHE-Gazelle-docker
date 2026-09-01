package net.ihe.gazelle.user.management.api.application.user.lookup;

import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.List;

/**
 * Search result for user summaries with pagination and total count.
 *
 * @param users  the list of users in the page
 * @param offset the offset of the result
 * @param limit  the page size
 * @param count  the total number of matching users
 */
public record UserSummarySearchResult(List<User> users, Integer offset, Integer limit, Long count) {
}
