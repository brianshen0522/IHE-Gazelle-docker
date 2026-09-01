package net.ihe.gazelle.maestro.client;

import java.net.URI;

public final class MaestroClientFactory {

    private MaestroClientFactory() {
    }

    public static MaestroClient createClient(String baseUrl,
                                             String k8sIdVariableName,
                                             int connectTimeout,
                                             int readTimeout) {
        return new MaestroHttpClient(
                URI.create(baseUrl),
                k8sIdVariableName,
                connectTimeout,
                readTimeout
        );
    }
}
