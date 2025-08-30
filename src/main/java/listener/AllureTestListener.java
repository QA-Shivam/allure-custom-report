package listener;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

public class AllureTestListener implements ITestListener {

    private static WebDriver driver; // inject driver from BaseTest

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
    }

    @Override
    public void onTestStart(ITestResult result) {
        Allure.step("Starting test: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Allure.step("Test passed: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Allure.step("Test failed: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            saveTextLog(result.getThrowable().toString());
        }
        if (driver != null) {
            saveScreenshot(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Allure.step("Test skipped: " + result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        Allure.step("Finished executing suite: " + context.getName());
        generateAllureReport();
    }

    private void generateAllureReport() {
        String resultsDir = "allure-results";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportsDir = "reports";
        String tempOutputDir = reportsDir + "/temp-allure-report-" + timestamp;
        String finalReportPath = reportsDir + "/allure-report-" + timestamp + ".html";

        try {
            // 🔹 Load allure.properties
            Properties props = new Properties();
            File propsFile = new File("src/test/resources/allure.properties");
            if (propsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(propsFile)) {
                    props.load(fis);
                }
            }

            String reportTitle = props.getProperty("allure.report.title", "");
            String cssPath = props.getProperty("allure.report.css", "");
            String jsPath = props.getProperty("allure.report.js", "");
            String faviconProp = props.getProperty("allure.report.favicon", "");

            // Detect OS
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c",
                        "allure", "generate", resultsDir,
                        "--clean", "--single-file", "-o", tempOutputDir);
            } else {
                pb = new ProcessBuilder("bash", "-c",
                        "allure generate " + resultsDir + " --clean --single-file -o " + tempOutputDir);
            }

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                File generatedIndex = new File(tempOutputDir, "index.html");
                File finalReport = new File(finalReportPath);

                if (generatedIndex.exists()) {
                    new File(reportsDir).mkdirs();

                    // Read HTML
                    String html = new String(Files.readAllBytes(generatedIndex.toPath()), StandardCharsets.UTF_8);

                    // 🔹 Replace Title
                    html = html.replace("<title>Allure Report</title>", "<title>" + reportTitle + "</title>");

                    // 🔹 CSS
                    if (!cssPath.isEmpty()) {
                        String css = new String(Files.readAllBytes(Paths.get(cssPath)), StandardCharsets.UTF_8);
                        html = html.replace("</head>", "<style>\n" + css + "\n</style>\n</head>");
                    }

                    // 🔹 JS
                    if (!jsPath.isEmpty()) {
                        String js = new String(Files.readAllBytes(Paths.get(jsPath)), StandardCharsets.UTF_8);
                        html = html.replace("</body>", "<script>\n" + js + "\n</script>\n</body>");
                    }

                    // 🔹 Favicon
                    String faviconTag = "";
                    if (!faviconProp.isEmpty()) {
                        if (faviconProp.startsWith("data:image")) {
                            faviconTag = "<link rel=\"icon\" href=\"" + faviconProp + "\"/>";
                        } else {
                            File faviconFile = new File(faviconProp);
                            if (faviconFile.exists()) {
                                byte[] bytes = Files.readAllBytes(faviconFile.toPath());
                                String base64 = Base64.getEncoder().encodeToString(bytes);
                                String mimeType = faviconFile.getName().endsWith(".png") ? "image/png" : "image/x-icon";
                                faviconTag = "<link rel=\"icon\" type=\"" + mimeType + "\" href=\"data:" + mimeType + ";base64," + base64 + "\"/>";
                            }
                        }
                    }
                    // remove old favicon and add new
                    html = html.replaceAll("<link[^>]*rel=[\"']icon[\"'][^>]*>", "");
                    if (!faviconTag.isEmpty()) {
                        html = html.replace("</head>", faviconTag + "\n</head>");
                    }

                    // Write back updated HTML
                    Files.write(finalReport.toPath(), html.getBytes(StandardCharsets.UTF_8));

                    // Cleanup
                    deleteDirectory(new File(tempOutputDir));

                    System.out.println("✅ Allure single-file report generated at: " + finalReport.getAbsolutePath());

                    // Auto open in browser
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(finalReport.toURI());
                    }

                } else {
                    System.err.println("⚠️ index.html not found in " + tempOutputDir);
                }
            } else {
                System.err.println("❌ Allure command failed. Exit code: " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Utility method to delete a directory recursively
     */
    private void deleteDirectory(File dir) throws IOException {
        if (dir.exists()) {
            Files.walk(dir.toPath())
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(File::delete);
        }
    }



    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] saveScreenshot(byte[] screenshot) {
        return screenshot;
    }

    @Attachment(value = "Failure Log", type = "text/plain")
    private String saveTextLog(String message) {
        return message;
    }
}
