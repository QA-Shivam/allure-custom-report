package base;
import listener.AllureTestListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.*;
import utility.ConfigReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws MalformedURLException {
        String browser = ConfigReader.get("browser");
        String baseUrl = ConfigReader.get("baseUrl");
        String runMode = ConfigReader.get("runMode");
        WebDriver webDriver;
        if (runMode.equalsIgnoreCase("grid")) {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName(browser);
            webDriver = new RemoteWebDriver(new URL(ConfigReader.get("gridUrl")), capabilities);
        } else {
            switch (browser.toLowerCase()) {
                case "firefox":
                    webDriver = new FirefoxDriver();
                    break;
                case "edge":
                    webDriver = new EdgeDriver();
                    break;
                default:
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless=new");
                    webDriver = new ChromeDriver(options);
            }
        }

        driver.set(webDriver);
        AllureTestListener.setDriver(getDriver());
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().get(baseUrl);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
        }
    }

    public static WebDriver getDriver() {
        return driver.get();
    }
}
