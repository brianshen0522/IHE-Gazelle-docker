package net.ihe.gazelle.validation.gateway.evs.technical.ws;

public class JsonProcessingException extends RuntimeException {
    public JsonProcessingException(String message) {
        super(message);
    }

    public JsonProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonProcessingException(Throwable cause) {
        super(cause);
    }
}
