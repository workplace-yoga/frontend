package com.adaptivetrust.qa.data;

public class TestData {
    public static final String VALID_ADMIN_EMAIL = "admin@adaptivetrust.io";
    public static final String VALID_ADMIN_PASSWORD = "Password@123";
    public static final String VALID_EMP_EMAIL = "employee@adaptivetrust.io";
    public static final String VALID_EMP_PASSWORD = "Password@123";
    public static final String VALID_INVITE_CODE = "ADAPT888";
    public static final String INVALID_EMAIL = "not-an-email";
    public static final String SHORT_PASSWORD = "123";
    public static final String SQL_INJECTION = "' OR '1'='1";
    public static final String XSS_PAYLOAD = "<script>alert(1)</script>";
    public static final String LONG_STRING_500 = "A".repeat(500);
}
