package tests;

import base.BaseTest;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoogleTest extends BaseTest {

    @Test(description = "Verify Google search works")
    @Severity(SeverityLevel.CRITICAL)               // Test priority
    @Owner("Shivam Kumar")                          // Test owner
    @Epic("Search Module")                          // Epic-level grouping
    @Feature("Google Search")                       // Feature grouping
    @Story("User is able to search for keywords")   // User story mapping
//    @Link(name = "Jira Ticket", url = "https://mytracker.com/issue/QA-101")  // Requirement link
    public void verifyTitle() {
        logStep("Opening Google homepage");
        String title = getDriver().getTitle();

        logStep("Page title fetched: " + title);

        Allure.parameter("Browser", System.getProperty("browser", "chrome"));
        Allure.parameter("Base URL", System.getProperty("baseUrl", "https://www.google.com"));

        logStep("Verifying if title contains 'Google'");
        Assert.assertTrue(title.contains("Google"), "❌ Title did not match!");
        logStep("✅ Title verification passed!");
    }

    @Step("{0}")
    public void logStep(String message) {
        // Step logging
    }
}
