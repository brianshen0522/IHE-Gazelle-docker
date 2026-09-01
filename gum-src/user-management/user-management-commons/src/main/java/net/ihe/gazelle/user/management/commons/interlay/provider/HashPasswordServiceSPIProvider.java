package net.ihe.gazelle.user.management.commons.interlay.provider;

import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;

import java.util.Optional;
import java.util.ServiceLoader;

public class HashPasswordServiceSPIProvider implements HashPasswordServiceProvider {

    public Optional<HashPasswordService> getHashPasswordService(String hashMethodName) {
        if (hashMethodName == null)
            throw new IllegalArgumentException("hashMethodName is null");
        // Usage of SPI to retrieve the correct implementation corresponding to the hash method stored in Credentials
        ServiceLoader<HashPasswordService> loader = ServiceLoader.load(HashPasswordService.class);
        Optional<ServiceLoader.Provider<HashPasswordService>> result = loader.stream().filter(
                hashPasswordServiceProvider ->
                        hashPasswordServiceProvider.get().getHashMethodName().equals(hashMethodName)
        ).findFirst();

        return result.map(ServiceLoader.Provider::get);
    }
}
