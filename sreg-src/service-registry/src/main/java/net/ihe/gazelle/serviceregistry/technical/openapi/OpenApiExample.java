package net.ihe.gazelle.serviceregistry.technical.openapi;

public class OpenApiExample {

    private OpenApiExample() {
        // Private constructor to prevent instantiation
    }

    public static final String EXAMPLE_DEPLOYED_SERVICE = """
            {
                                    "name":"XML Validator",
                                    "version":"3.2.1",
                                    "instanceId":"1a2b3",
                                    "replicaId":"c4d",
                                    "providedInterfaces":[
                                       {
                                          "interfaceName":"Validation Service API",
                                          "interfaceVersion":"1.0",
                                          "bindings":[{"type":"REST","serviceUrl":"http://localhost:8080/xml-validator/rest"}]
                                       }
                                    ],
                                    "consumedInterfaces":[
                                       {
                                          "interfaceName":"SVS Repository API",
                                          "required":false,
                                          "supportedVersions":["1.0","2.0"],
                                          "supportedBindings":["REST","HTTP_SOAP"]
                                       }
                                    ]
                                 }
            """;

    public static final String EXAMPLE_DEPLOYED_SERVICES = "[" + EXAMPLE_DEPLOYED_SERVICE + "]";

}
