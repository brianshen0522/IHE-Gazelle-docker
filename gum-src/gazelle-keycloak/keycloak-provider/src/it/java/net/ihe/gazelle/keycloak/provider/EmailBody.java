package net.ihe.gazelle.keycloak.provider;

public class EmailBody {

    private final String bodyPlainText;
    private final String bodyHtml;
    public EmailBody(String body) {
        bodyPlainText=body.substring(0,body.indexOf("Content-Type: text/html"));
        bodyHtml=body.substring(body.indexOf("<html>"),body.indexOf("</html>"));
    }

    public String getBodyPlainText() {
        return bodyPlainText;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }
}
