package net.ihe.gazelle.user.management.commons.application.user.login;

import java.util.Optional;

public interface HashPasswordServiceProvider {

    Optional<HashPasswordService> getHashPasswordService(String hashMethodName);
}
