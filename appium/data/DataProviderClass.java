package com.adaptivetrust.qa.data;

import org.testng.annotations.DataProvider;

public class DataProviderClass {
    @DataProvider(name = "invalidLoginData")
    public static Object[][] getInvalidLoginData() {
        return new Object[][] {
            {"", "Password@123", "Email required"},
            {"admin@adaptivetrust.io", "", "Password required"},
            {"invalid-email", "Password@123", "Invalid email format"},
            {"notfound@adaptivetrust.io", "WrongPass123", "Invalid credentials"}
        };
    }
}
