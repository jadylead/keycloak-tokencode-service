package net.interus.keycloak.tokencode.relay;

public enum MediaType {
    SMS("SMS"),
    LMS("LMS"),
    CALL("CALL"),
    EMAIL("EMAIL"),
    BIZTALK("BIZTALK"),
    PUSH_NOTIFICATION("PUSH_NOTIFICATION"),
    TEST("TEST");

    private String label;

    public String getLabel() {
        return label;
    }

    MediaType(String label) {
        this.label  = label;
    }
}
