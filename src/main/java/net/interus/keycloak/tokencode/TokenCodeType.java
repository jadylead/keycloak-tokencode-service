package net.interus.keycloak.tokencode;

public enum TokenCodeType {
    OTP("OTP"),
    OTP_SAFE("OTP_SAFE");

    private String label;

    public String getLabel() {
        return label;
    }

    TokenCodeType(String label) {
        this.label  = label;
    }
}
