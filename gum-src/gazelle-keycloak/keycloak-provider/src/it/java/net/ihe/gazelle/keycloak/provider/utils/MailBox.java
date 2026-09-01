package net.ihe.gazelle.keycloak.provider.utils;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParser;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MailBox {

    HttpClient httpClient;

    public MailBox() {
        httpClient = HttpClient.newBuilder().build();
    }

    public JsonNode getLastMailAsJson() {
        String mailServerUrl = System.getProperty("it.base.uri","http://localhost") +":8025/";
        // Get last message
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(mailServerUrl + "api/v2/messages")).build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = HttpResponseToJson(httpResponse).get("items").get(0);

            // If no message, return null
            if (jsonNode == null)
                return null;

            // Otherwise delete last message
            HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create(mailServerUrl +"api/v1/messages/" + jsonNode.get("ID").asText())).DELETE().build();            httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
            return jsonNode.get("Content");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode HttpResponseToJson(HttpResponse<String> httpResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (JsonParser p = mapper.getFactory().createParser(httpResponse.body())) {
            return p.readValueAsTree();
        }
    }

}

