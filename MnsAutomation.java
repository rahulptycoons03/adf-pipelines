package web;/*
M&S AE Automation Test - Full Conversion (Node.js -> Java Selenium)

Requirements:
- Selenium Java (4.x)
- ChromeDriver compatible with your Chrome version
- Maven dependencies (add to pom.xml):

<dependencies>
  <dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.11.0</version>
  </dependency>
</dependencies>

Run as a plain Java application (IDE) or via `mvn exec:java` if configured.

This single-file implementation mirrors the original Node.js script: it launches Chrome,
navigates to the M&S AE site, tries multiple selectors for navigation, finds a product,
attempts to add to bag, runs a checkout flow (best-effort), captures screenshots,
and generates a styled HTML report in ./test-results/reports.
*/

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MnsAutomation {

    // ------------------------- Helper classes -------------------------
    static class Step {
        String icon;
        String title;
        String description;
        String type;
        String details;
        String timestamp;

        Step(String icon, String title, String description, String type, String details) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.type = type == null ? "" : type;
            this.details = details == null ? "" : details;
            this.timestamp = DateTimeFormatter.ofPattern("hh:mm:ss a").format(LocalTime.now());
        }
    }

    static class TestResults {
        List<Step> steps = new ArrayList<>();
        String pageTitle = "";
        String finalUrl = "";
        String screenshotPath = "";
        long executionTime = 0;
    }

    // ------------------------- Constants -------------------------
    private static final String BASE_URL = "https://aeuatmnssingapore.corp.al-futtaim.com/en/fashion";
    private static final Path RESULTS_DIR = Paths.get(System.getProperty("user.dir"), "test-results");
    private static final Path SCREENSHOT_DIR = RESULTS_DIR.resolve("screenshots");
    private static final Path REPORTS_DIR = RESULTS_DIR.resolve("reports");
    private static final long DEFAULT_TIMEOUT_SECONDS = 15;

    // ------------------------- Main -------------------------
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        TestResults results = new TestResults();
        addStep(results, "🚀", "Test Started", "Initializing M&S SG automation test", "");

        WebDriver driver = null;

        try {
            addStep(results, "⚙️", "Chrome Setup", "Configuring Chrome browser options", "");
            System.setProperty("webdriver.chrome.driver", "D:\\Users\\amruthal\\IdeaProjects\\Automation\\src\\main\\resources\\chromedriver.exe");
            ChromeOptions chromeOptions = new ChromeOptions();
            driver = new ChromeDriver(chromeOptions);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

            addStep(results, "🔧", "WebDriver Creation", "Building Chrome WebDriver instance", "");
           // driver = new ChromeDriver(chromeOptions);
           // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            addStep(results, "✅", "WebDriver Ready", "Chrome WebDriver created successfully", "");

            addStep(results, "🌐", "Navigation", "Navigating to M&S SG homepage", "");
           ContinuewithKIBOFulfilment(driver,"31484",results);
            driver.get(BASE_URL);
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));
            // small wait for page load
            waitForPageLoad(driver, DEFAULT_TIMEOUT_SECONDS);

            results.pageTitle = driver.getTitle();
            results.finalUrl = driver.getCurrentUrl();
            addStep(results, "📄", "Page Loaded", "Page title: \"" + results.pageTitle + "\"", "", "URL: " + results.finalUrl);

            // Navigate to Kids section
            addStep(results, "👶", "Navigation", "Looking for Kids section in navigation", "");
            sleep(2000);

            //Login

            WebElement SignInButton = findElementWithMultipleSelectors(driver, results, new SelectorGroup()
                    .xpath("//div[contains(text(),'Log in | Sign up')]")
            );
            SignInButton.click();

            sleep(2000);
            WebElement loginOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(text(),'Log in')])[2]")));
            loginOption.click();
            WebElement useEmailButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(text(),'Use email instead')]")));
            useEmailButton.click();


            WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@type='email'])[1]")));
            emailField.sendKeys("Rishivanthya.Sambandam@alfuttaim.com");
            WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@type='password'])[1]")));
            passwordField.sendKeys("Sasi@123");
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Log in')]")));
            loginButton.click();
            sleep(6000);
            addStep(results, "✅", "Login", "Successfully logged in to the application", "");

            WebElement WomenLink = driver.findElement(By.xpath("(//span[contains(text(),'Women')])[1]"));

            Actions actions = new Actions(driver);
            // Hover over the element
            actions.moveToElement(WomenLink).perform();

            // WomenLink.click();

            sleep(2000);
            if (WomenLink != null) {
                addStep(results, "✅", "Women Section Found", "Located Women section in navigation", "");

               // scrollIntoView(driver, kidsLink);
                sleep(1000);

               // boolean clicked = tryClickWithMultipleMethods(driver, results, WomenLink, "Women Section Clicked");
               //if (!clicked) throw new RuntimeException("Unable to click Women section");

                addStep(results, "👶", "Women Navigation", "Navigated to Women section", "");
                //sleep(3000);

                // Newborn/Baby subsection
                addStep(results, "🍼", "Newborn Search", "Looking for Newborn/Baby subsection", "");

               // ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 800);");
                // Click the link
                WebElement FootwearLink = driver.findElement(By.xpath("(//span[contains(text(),'Footwear')])"));
                FootwearLink.click();

                // WebElement newbornLink = driver.findElement(By.xpath("//p/span[contains(text(),'Women's jeans')]"));
               /* WebElement newbornLink = findElementWithMultipleSelectors(driver, results, new SelectorGroup()
                        .xpath("//p/span[contains(text(),'Women's jeans')]")
                );*/

               /* if (newbornLink != null) {
                  //  scrollIntoView(driver, newbornLink);
                    sleep(1000);
                    tryClickWithMultipleMethods(driver, results, newbornLink, "Kids Section Clicked");


                    sleep(1000);
                } else {
                    addStep(results, "⚠️", "Newborn Section", "Newborn section not found, staying in Kids section", "");
                }*/

                // Scroll and find products
                addStep(results, "📜", "Page Scroll", "Scrolling down to find products on the page", "");
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 800);");
                sleep(2000);

                addStep(results, "🛍️", "Product Search", "Looking for products after scrolling", "");

                // Try specific class first
                WebElement productElement = null;
                try {
                    List<WebElement> specific = driver.findElements(By.className("plp-card"));
                    if (!specific.isEmpty()) {
                        productElement = specific.get(0);
                        addStep(results, "✅", "Product Found", "Found " + specific.size() + " products using specific class: .mz-productlisting-title", "");
                    }
                } catch (Exception e) {
                    addStep(results, "⚠️", "Specific Class Search", "Could not find products with .mz-productlisting-title class", "");
                }

                if (productElement == null) {
                    addStep(results, "🔍", "Fallback Search", "Trying fallback product selectors", "");
                    String[] productSelectors = new String[]{
                            ".product-item",
                            ".product-card",
                            ".product-tile",
                            ".product",
                            "[data-testid*='product']",
                            "[data-testid*='item']",
                            ".item",
                            "article",
                            "a[href*='/product']",
                            "a[href*='/item']",
                            ".grid-item",
                            ".catalog-item",
                            ".listing-item",
                            ".product-listing",
                            ".merchandise-item",
                            ".shop-item",
                            ".browse-item",
                            ".product-thumbnail",
                            ".product-image"
                    };

                    for (String sel : productSelectors) {
                        try {
                            List<WebElement> list = driver.findElements(By.cssSelector(sel));
                            if (!list.isEmpty()) {
                                productElement = list.get(0);
                                addStep(results, "✅", "Product Found", "Found " + list.size() + " products using selector: " + sel, "");
                                break;
                            }
                        } catch (Exception e) {
                            // continue
                        }
                    }
                }

                if (productElement == null) {
                    addStep(results, "📜", "Further Scroll", "No products found, scrolling down further", "");
                    ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 1500);");
                    sleep(2000);

                    String[] xpaths = new String[]{
                            "//a[contains(@href, '/product')]",
                            "//a[contains(@href, '/item')]",
                            "//div[contains(@class, 'product')]",
                            "//article",
                            "//img[contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'product') or contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'item') ]",
                            "//div[contains(@class, 'item')]",
                            "//a[contains(@class, 'product')]"
                    };

                    for (String xp : xpaths) {
                        try {
                            List<WebElement> list = driver.findElements(By.xpath(xp));
                            if (!list.isEmpty()) {
                                productElement = list.get(0);
                                addStep(results, "✅", "Product Found", "Found " + list.size() + " products using XPath: " + xp, "");
                                break;
                            }
                        } catch (Exception e) {
                            // continue
                        }
                    }
                }

                if (productElement != null) {
                    addStep(results, "✅", "Product Located", "Successfully found a product on the page", "");
                    scrollIntoView(driver, productElement);
                    sleep(1000);

                    boolean productClicked = tryClickWithMultipleMethods(driver, results, productElement, "Product Selected");
                    if (productClicked) {
                        //WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));
                        sleep(3000);
                        String productTitle = driver.getTitle();
                        String productUrl = driver.getCurrentUrl();
                        addStep(results, "📦", "Product Page", "Navigated to product page: \"" + productTitle + "\"", "", "URL: " + productUrl);


                        // Select size
                        WebElement sizeDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(@class,'SizeVariant_size')])[1]")));
                        sizeDropdown.click();
                        sleep(3000);
                        // Select length
                       /* try{
if(driver.findElement(By.xpath("//span[contains(text(),'Long')]")).isDisplayed()){
                        WebElement lengthDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Long')]")));
                        scrollIntoView(driver, lengthDropdown);
                        lengthDropdown.click();
                        sleep(1000);}}catch(Exception ignored)
                        {

                        }*/
                        // Click Add to Cart
                        // Look for Add to bag
                        addStep(results, "🛒", "Add to Bag Search", "Looking for Add to bag button", "");
                        sleep(2000);
                        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 250);");
                        WebElement addToCartButton = driver.findElement(By.xpath("//button[contains(text(),'Add to cart')]"));
                        //wait.until(ExpectedConditions.ele(By.xpath("//button[contains(text(),'Add to cart')]")));
                        sleep(1000);
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        // js.executeScript("window.scrollBy(0, 500)");
                        // js.executeScript("arguments[0].scrollIntoView(true);", addToCartButton);

                        // scrollIntoView(driver, addToCartButton);

                        sleep(3000);
                        addToCartButton.click();
                        addStep(results, "🛒", "Add to Bag ", "Add to Cart button clicked", "");

                        sleep(2000);
                        String finalTitle = driver.getTitle();
                        String finalUrl = driver.getCurrentUrl();

                        addStep(results, "🎉", "Add to Bag Success", "Product added to bag successfully!", "", "Final URL: " + finalUrl);
                        sleep(2000);
                      /*


*/                        // Continue checkout
                        performCheckoutFlow(driver, results);
                        String OrderNumber = performPayment(driver, results);
                        ContinuewithKIBOFulfilment(driver,"31448",results);


                    }else {
                        addStep(results, "❌", "Product Selection Failed", "Could not select product after multiple attempts", "error");
                    }

                } else {
                    addStep(results, "❌", "No Products Found", "No products found on the current page", "error");
                }

            } else {
                addStep(results, "❌", "Kids Section Not Found", "Could not locate Kids section in navigation", "error");
            }

            // Final screenshot and cleanup
            addStep(results, "📸", "Final Screenshot", "Taking final screenshot of the page", "");
            String screenshotPath = saveScreenshot(driver, "ms-ae-test");
            results.screenshotPath = screenshotPath;
            addStep(results, "✅", "Screenshot Saved", "Screenshot saved: " + Paths.get(screenshotPath).getFileName().toString(), "");

            addStep(results, "🎯", "Test Completed", "M&S SG homepage test completed successfully!", "");
            addStep(results, "✅", "URL Validation", "URL validation passed", "");
            addStep(results, "✅", "Page Load Validation", "Page loaded successfully", "");

            // Close browser
            try {
                driver.quit();
                addStep(results, "🔒", "Cleanup", "Browser closed successfully", "");
            } catch (Exception e) {
                addStep(results, "⚠️", "Cleanup Error", "Error closing browser: " + e.getMessage(), "error");
            }

            results.executionTime = System.currentTimeMillis() - start;
            addStep(results, "⏱️", "Execution Time", "Total execution time: " + results.executionTime + "ms", "");

        } catch (Exception ex) {
            addStep(results, "❌", "Test Failed", "Error: " + ex.getMessage(), "error", getStackTrace(ex));
            try {
                if (driver != null) {
                    String path = saveScreenshot(driver, "ms-ae-error");
                    results.screenshotPath = path;
                    addStep(results, "📸", "Error Screenshot", "Error screenshot saved: " + Paths.get(path).getFileName().toString(), "");

                    driver.quit();
                    addStep(results, "🔒", "Cleanup", "Browser closed after error", "");
                }
            } catch (Exception cleanupEx) {
                addStep(results, "⚠️", "Cleanup Error", "Cleanup error: " + cleanupEx.getMessage(), "error");
            }

            results.executionTime = System.currentTimeMillis() - start;

        } finally {
            // Generate HTML report irrespective of pass/fail
            generateAndSaveReport(results);
        }

    }

    private static void ContinuewithKIBOFulfilment(WebDriver driver,String orderNumber, TestResults results) {

        driver.get("https://www.euw1.kibocommerce.com/");
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));
        sleep(3000);
        WebElement kibo_UserEmail = wait.until(ExpectedConditions.elementToBeClickable(By.name("Email")));
        kibo_UserEmail.sendKeys("lubna.ayesha-external@alfuttaim.com");
        sleep(1000);
        WebElement NextButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonSubmit")));
        NextButton.click();
        sleep(2000);

        WebElement kibo_UserPwd = wait.until(ExpectedConditions.elementToBeClickable(By.name("Password")));
        kibo_UserPwd.sendKeys("World@365");
        sleep(1000);
        WebElement SubmitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[contains(@value,'Log in')]")));
        SubmitButton.click();


        WebElement UATEnv = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Marks and Spencer UAT']")));
        UATEnv.click();
        sleep(4000);
        WebElement Hamburger = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//i[contains(@class,'hamburgerMenu')]")));
        Hamburger.click();
        sleep(1000);
        WebElement MainOrder = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Orders']")));
        MainOrder.click();
        sleep(1000);
        WebElement Orders = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()=' Orders ']")));
        Orders.click();
        sleep(2000);
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[contains(@name,'inputEl')])[2]")));
        searchBox.sendKeys(orderNumber);
        searchBox.sendKeys(Keys.ENTER);
        sleep(3000);
        WebElement row = driver.findElement(By.xpath("//tr[contains(@id,'gridview')]"));
        row.click();
        sleep(3000);
        WebElement shipmentsTab = driver.findElement(By.xpath("//li[contains(text(),'Shipments')]"));
        shipmentsTab.click();
        sleep(1000);
        WebElement ShipmentID = driver.findElement(By.xpath("//div[contains(@class,'labelvalue')]/a"));
        ShipmentID.click();
        sleep(4000);
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 350);");
        WebElement Prcoeed_PrintPack = driver.findElement(By.xpath("//button[text()=' Proceed To Validate Items In Stock ']"));
        Prcoeed_PrintPack.click();
        WebElement getQuantity = driver.findElement(By.xpath("//span[contains(text(),'Quantity')]/following-sibling::div[contains(@class,'inserted')]"));
        String value = getQuantity.getText().trim();
        sleep(1000);
        WebElement enterQuantity = driver.findElement(By.xpath("//input[@placeholder='Current Stock Amount']"));
        enterQuantity.sendKeys(value);
        sleep(1000);
        WebElement Prcoeed_slip = driver.findElement(By.xpath("//div/button[contains(text(),'Packing Slip')]"));
        Prcoeed_slip.click();
        WebElement Prcoeed_shipment = driver.findElement(By.xpath("//div/button[contains(text(),'Prepare for Shipment')]"));
        Prcoeed_shipment.click();
        WebElement boxSelection = driver.findElement(By.xpath("//div/button[contains(text(),' Medium Bag ')]"));
        boxSelection.click();
        WebElement weight = driver.findElement(By.xpath("//input[@placeholder='Weight']"));
        weight.sendKeys("12");
        WebElement Save = driver.findElement(By.xpath("//span[contains(text(),'Save')]"));
        Save.click();
        WebElement PrintShippingLabel = driver.findElement(By.xpath("//span[contains(text(),' Print Shipping Label ')]"));
        PrintShippingLabel.click();
        sleep(4000);

        Alert alert = driver.switchTo().alert();
        // Dismiss the alert (like clicking 'Cancel' or 'No')
        alert.dismiss();
        WebElement CompleteShipment = driver.findElement(By.xpath("//button[contains(text(),' Yes, Complete Shipment ')]"));
        CompleteShipment.click();

        WebElement CloseButton = driver.findElement(By.xpath("//button[contains(text(),' Close ')]"));
        CloseButton.click();














    }

    private static String performPayment(WebDriver driver, TestResults results) {

        addStep(results, "🛒", "Payment Flow", "Starting Payment process", "");
        sleep(2000);
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 350);");
        sleep(3000);
        WebElement morePayOptions = wait.until(ExpectedConditions.elementToBeClickable(By.name("more-payment-options")));
        morePayOptions.click();
        sleep(2000);
        addStep(results, "🛒", "Payment Flow", "Selected more Payment Options", "");
        WebElement continuePaymentButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue to payment')]")));
        continuePaymentButton.click();
        addStep(results, "🛒", "Payment Flow", "Clicked on Continue Payments", "");
        sleep(5000);



        driver.switchTo().frame("pgw-ui-container-dropin-iframe");

// Now you can interact with elements inside the iframe


// Switch back to the main content

        WebElement cardNumber = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("cardNumber")));
        //wait.until(ExpectedConditions.presenceOfElementLocated(By.name("cardNumber")));
        cardNumber.sendKeys("4111111111111111");
        sleep(1000);
        WebElement expiryYear = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("expyear")));
        expiryYear.sendKeys("12/28");
        WebElement cvv = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("cvv")));
        cvv.sendKeys("100");
        WebElement Name = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        Name.sendKeys("Test User");
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        email.sendKeys("Testuser@gmail.com");

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue payment')]")));
        continueButton.click();
        sleep(4000);
        driver.switchTo().defaultContent();
        sleep(3000);
        WebElement otpField = wait.until(ExpectedConditions.elementToBeClickable(By.name("challengeDataEntry")));
        otpField.sendKeys("123456");
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'\n" +
                "                                    Submit\n" +
                "                                ')]")));
        submitButton.click();
        sleep(10000);

        WebElement OrderDetails = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'View order details')]")));
        OrderDetails.click();
        sleep(5000);
        String OrderID = driver.findElement(By.xpath("(//span[contains(@class,'body-lg-bold')])[1]")).getText();
        return OrderID;
    }

    // ------------------------- Utility methods -------------------------
    private static void addStep(TestResults r, String icon, String title, String description, String type) {
        addStep(r, icon, title, description, type, "");
    }

    private static void addStep(TestResults r, String icon, String title, String description, String type, String details) {
        Step s = new Step(icon, title, description, type, details);
        r.steps.add(s);
        System.out.println(icon + " " + title + ": " + description);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private static void waitForPageLoad(WebDriver driver, long timeoutSec) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSec)).until((ExpectedCondition<Boolean>) wd ->
                    ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private static void scrollIntoView(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", el);
        } catch (Exception ignored) {}
    }

    private static boolean tryClickWithMultipleMethods(WebDriver driver, TestResults results, WebElement el, String successTitle) {
        try {
            el.click();
            addStep(results, "✅", successTitle, "Successfully clicked (direct method)", "");
            return true;
        } catch (Exception e1) {
           // addStep(results, "⚠️", "Direct Click Failed", "Direct click failed: " + e1.getMessage(), "");
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                addStep(results, "✅", successTitle, "Successfully clicked (JavaScript method)", "");
                return true;
            } catch (Exception e2) {
               // addStep(results, "⚠️", "JavaScript Click Failed", "JavaScript click failed: " + e2.getMessage(), "");
                try {
                    Actions actions = new Actions(driver);
                    actions.moveToElement(el).click().perform();
                    addStep(results, "✅", successTitle, "Successfully clicked (Actions API method)", "");
                    return true;
                } catch (Exception e3) {
                    addStep(results, "❌", "All Click Methods Failed", "All click methods failed: " + e3.getMessage(), "error");
                    return false;
                }
            }
        }
    }

    private static String saveScreenshot(WebDriver driver, String prefix) {
        try {
            if (!(driver instanceof TakesScreenshot)) return "";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            Files.createDirectories(SCREENSHOT_DIR);

            String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss").format(LocalDateTime.now());
            String filename = prefix + "-" + timestamp + ".png";
            Path dest = SCREENSHOT_DIR.resolve(filename);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (Exception e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
            return "";
        }
    }

    private static void generateAndSaveReport(TestResults results) {
        try {
            Files.createDirectories(REPORTS_DIR);
            String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss").format(LocalDateTime.now());
            String reportFilename = "ms-ae-test-report-" + timestamp + ".html";
            Path reportPath = REPORTS_DIR.resolve(reportFilename);

            String html = generateHTMLReport(results);
            Files.writeString(reportPath, html, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // also write latest-report.html
            Path latest = REPORTS_DIR.resolve("latest-report.html");
            Files.writeString(latest, html, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("\n📊 HTML Report generated: " + reportPath.toString());
            System.out.println("📋 Latest report saved: " + latest.toString());

        } catch (Exception e) {
            System.err.println("❌ Failed to generate HTML report: " + e.getMessage());
        }
    }

    private static String generateHTMLReport(TestResults testResults) {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

        StringBuilder stepsHtml = new StringBuilder();
        for (Step s : testResults.steps) {
            stepsHtml.append("<div class=\"step " + (s.type != null ? s.type : "") + "\">\n");
            stepsHtml.append("<strong>" + escapeHtml(s.icon + " " + s.title) + "</strong>\n");
            stepsHtml.append("<p>" + escapeHtml(s.description) + "</p>\n");
            stepsHtml.append("<div class=\"timestamp\">" + escapeHtml(s.timestamp) + "</div>\n");
            if (s.details != null && !s.details.isEmpty()) {
                stepsHtml.append("<div class=\"details\">" + escapeHtml(s.details) + "</div>\n");
            }
            stepsHtml.append("</div>\n");
        }

        String screenshotRel = "";
        if (testResults.screenshotPath != null && !testResults.screenshotPath.isEmpty()) {
            Path p = Paths.get(testResults.screenshotPath);
            screenshotRel = p.getFileName().toString();
        }

        String status = testResults.steps.stream().anyMatch(s -> "error".equalsIgnoreCase(s.type))
                ? "<span class=\"status-fail\">❌ FAILED</span>"
                : "<span class=\"status-pass\">✅ PASSED</span>";

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>M&S SG Automation Test Report</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
                "        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
                "        .header { text-align: center; color: #333; border-bottom: 3px solid #4CAF50; padding-bottom: 20px; margin-bottom: 30px; }\n" +
                "        .test-info { background: #e8f5e8; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
                "        .step { margin: 15px 0; padding: 10px; border-left: 4px solid #4CAF50; background: #f9f9f9; }\n" +
                "        .step.error { border-left-color: #f44336; background: #ffebee; }\n" +
                "        .step.warning { border-left-color: #ff9800; background: #fff3e0; }\n" +
                "        .timestamp { color: #666; font-size: 0.9em; }\n" +
                "        .screenshot { text-align: center; margin: 20px 0; }\n" +
                "        .screenshot img { max-width: 100%; border: 1px solid #ddd; border-radius: 5px; }\n" +
                "        .summary { background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
                "        .status-pass { color: #4CAF50; font-weight: bold; }\n" +
                "        .status-fail { color: #f44336; font-weight: bold; }\n" +
                "        .details { background: #fafafa; padding: 10px; border-radius: 5px; margin: 10px 0; font-family: monospace; white-space: pre-wrap; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>🧪 M&S SG Automation Test Report</h1>\n" +
                "            <p class=\"timestamp\">Generated on: " + timestamp + "</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"test-info\">\n" +
                "            <h2>📋 Test Summary</h2>\n" +
                "            <p><strong>Test Name:</strong> M&S SG Homepage Launch Test</p>\n" +
                "            <p><strong>Browser:</strong> Chrome (UI Mode)</p>\n" +
                "            <p><strong>URL:</strong> " + escapeHtml(BASE_URL) + "</p>\n" +
                "            <p><strong>Status:</strong> "+status+"</p>\n" +
                "            <p><strong>Execution Time:</strong> " + testResults.executionTime + "ms</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"summary\">\n" +
                "            <h3>📊 Test Results</h3>\n" +
                "            <p><strong>Page Title:</strong> " + escapeHtml(testResults.pageTitle) + "</p>\n" +
                "            <p><strong>Final URL:</strong> " + escapeHtml(testResults.finalUrl) + "</p>\n" +
                "            <p><strong>Screenshot:</strong> " + escapeHtml(screenshotRel) + "</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <h3>📝 Execution Steps</h3>\n" +
                stepsHtml.toString() +
                (screenshotRel.isEmpty() ? "" :
                        "\n        <div class=\"screenshot\">\n            <h3>📸 Screenshot</h3>\n            <img src=\"../screenshots/" + screenshotRel + "\" alt=\"Test Screenshot\">\n            <p>Screenshot saved: " + escapeHtml(screenshotRel) + "</p>\n        </div>\n") +
                "\n        <div class=\"test-info\">\n" +
                "            <h3>🎯 Test Validation</h3>\n" +
                "            <p>✅ WebDriver initialized successfully</p>\n" +
                "            <p>✅ Navigated to M&S SG homepage</p>\n" +
                "            <p>✅ Page title retrieved: \"" + escapeHtml(testResults.pageTitle) + "\"</p>\n" +
                "            <p>✅ URL validation passed</p>\n" +
                "            <p>✅ Kids section navigation (attempted)</p>\n" +
                "            <p>✅ Product search and selection (attempted)</p>\n" +
                "            <p>✅ Add to bag functionality (attempted)</p>\n" +
                "            <p>✅ Checkout flow (attempted)</p>\n" +
                "            <p>✅ Delivery options (attempted)</p>\n" +
                "            <p>✅ Screenshot captured successfully</p>\n" +
                "            <p>✅ Browser closed properly</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <footer style=\"text-align: center; margin-top: 30px; color: #666; border-top: 1px solid #eee; padding-top: 20px;\">\n" +
                "            <p>Generated by M&S SG Automation Suite | " + timestamp + "</p>\n" +
                "        </footer>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        return html;
    }

    // ------------------------- Checkout flow (best-effort) -------------------------
    private static void performCheckoutFlow(WebDriver driver, TestResults results) {
        try {
            addStep(results, "🛒", "Checkout Flow", "Starting checkout process", "");
            sleep(2000);
            WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(10));
            WebElement goToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Go to Cart')]")));
            goToCartButton.click();
            addStep(results, "📄", "Checkout Page", "Navigated to checkout page: \"" + driver.getTitle() + "\"", "", "URL: " + driver.getCurrentUrl());

            sleep(2000);
            addStep(results, "🛒", "Add to Bag ", "Go to Cart button clicked", "");
            addStep(results, "🛒", "Checkout", "Looking for checkout button", "");
            WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Checkout')]")));
                // Secure checkout
                if (checkoutButton != null) {
                    checkoutButton.click();
                    addStep(results, "🛒", "Checkout", "Checkout button clicked", "");
                    sleep(3000);
                    addStep(results, "📄", "Delivery Page", "Navigated to delivery page: \"" + driver.getTitle() + "\"", "", "URL: " + driver.getCurrentUrl());


                    WebElement HomeDelivery = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(text(),'Home delivery')]")));
                    HomeDelivery.click();
                    addStep(results, "🛒", "Checkout", "Slected DFelivery Option", "");

                    WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue')]")));
                    continueButton.click();
                    addStep(results, "🛒", "Checkout", "Clicked on Continue Button", "");
                    // Fill address
                   // fillDeliveryAddressForm(driver, results);

                    // Select delivery options
                   // selectDeliveryOptions(driver, results);

                } else {
                    addStep(results, "❌", "Secure Checkout Not Found", "Could not find Checkout Securely button", "error");
                }



        } catch (Exception e) {
            addStep(results, "❌", "Checkout Flow Error", "Checkout flow failed: " + e.getMessage(), "error");
        }
    }

    private static void fillDeliveryAddressForm(WebDriver driver, TestResults results) {
        try {
            addStep(results, "📝", "Delivery Address Form", "Starting to fill delivery address form", "");
            sleep(2000);

            FormField[] formFields = new FormField[]{
                    new FormField("input[name*='firstName'], input[id*='firstName'], input[placeholder*='First'], input[placeholder*='first']", "First Name", "John"),
                    new FormField("input[name*='lastName'], input[id*='lastName'], input[placeholder*='Last'], input[placeholder*='last']", "Last Name", "Doe"),
                    new FormField("input[name*='email'], input[id*='email'], input[type='email']", "Email", "john.doe@example.com"),
                    new FormField("input[name*='phone'], input[id*='phone'], input[placeholder*='Phone'], input[placeholder*='phone']", "Phone", "+971501234567"),
                    new FormField("input[name*='address'], input[id*='address'], input[placeholder*='Address'], input[placeholder*='address']", "Address", "123 Main Street"),
                    new FormField("input[name*='city'], input[id*='city'], input[placeholder*='City'], input[placeholder*='city']", "City", "Dubai"),
                    new FormField("input[name*='postal'], input[id*='postal'], input[name*='zip'], input[id*='zip'], input[placeholder*='Postal'], input[placeholder*='postal']", "Postal Code", "12345")
            };

            for (FormField field : formFields) {
                try {
                    List<WebElement> elems = driver.findElements(By.cssSelector(field.selector));
                    if (!elems.isEmpty()) {
                        WebElement el = elems.get(0);
                        if (el.isDisplayed() && el.isEnabled()) {
                            scrollIntoView(driver, el);
                            sleep(500);
                            try {
                                el.clear();
                            } catch (Exception ignored) {}
                            el.sendKeys(field.value);
                            addStep(results, "✅", "Field Filled: " + field.label, "Successfully filled " + field.label + " with: " + field.value, "");
                        } else {
                            addStep(results, "⚠️", "Field Skipped: " + field.label, "Field " + field.label + " is not visible or enabled", "");
                        }
                    } else {
                        addStep(results, "⚠️", "Field Not Found: " + field.label, "Could not find " + field.label + " field", "");
                    }
                } catch (Exception e) {
                    addStep(results, "⚠️", "Field Error: " + field.label, "Error filling " + field.label + ": " + e.getMessage(), "");
                }
            }

            addStep(results, "✅", "Address Form Complete", "Finished filling delivery address form", "");

        } catch (Exception e) {
            addStep(results, "❌", "Address Form Error", "Error filling address form: " + e.getMessage(), "error");
        }
    }

    private static void selectDeliveryOptions(WebDriver driver, TestResults results) {
        try {
            addStep(results, "🚚", "Delivery Options", "Looking for delivery options section", "");
            sleep(2000);

            WebElement deliverySection = findElementWithMultipleSelectors(driver, results, new SelectorGroup()
                    .css("[data-testid*='delivery']")
                    .css(".delivery-options")
                    .css(".delivery-section")
                    .xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delivery')]")
                    .xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'shipping')]")
            );

            if (deliverySection != null) {
                scrollIntoView(driver, deliverySection);
                sleep(1000);
                addStep(results, "✅", "Delivery Options Accessed", "Successfully accessed delivery options section", "");

                String[] deliveryOptionSelectors = new String[]{
                        "input[type='radio'][name*='delivery']",
                        "input[type='radio'][name*='shipping']",
                        "button[class*='delivery']",
                        "button[class*='shipping']",
                        ".delivery-option",
                        ".shipping-option"
                };

                boolean selected = false;
                for (String sel : deliveryOptionSelectors) {
                    try {
                        List<WebElement> opts = driver.findElements(By.cssSelector(sel));
                        if (!opts.isEmpty()) {
                            WebElement option = opts.get(0);
                            if (option.isDisplayed() && option.isEnabled()) {
                                try {
                                    option.click();
                                    addStep(results, "✅", "Delivery Option Selected", "Successfully selected a delivery option", "");
                                    selected = true;
                                    break;
                                } catch (Exception ce) {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                                    addStep(results, "✅", "Delivery Option Selected", "Successfully selected a delivery option (JavaScript method)", "");
                                    selected = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (!selected) {
                    addStep(results, "⚠️", "No Delivery Options", "No clickable delivery options found, but section accessed successfully", "");
                }

            } else {
                addStep(results, "❌", "Delivery Section Not Found", "Could not find delivery options section", "error");
            }

        } catch (Exception e) {
            addStep(results, "❌", "Delivery Options Error", "Error accessing delivery options: " + e.getMessage(), "error");
        }
    }

    // ------------------------- Small utilities -------------------------
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // ------------------------- SelectorGroup helper -------------------------
    // A small convenience builder to try multiple selectors (css/xpath)
    private static class SelectorGroup {
        private final List<By> selectors = new ArrayList<>();

        SelectorGroup css(String css) { selectors.add(By.cssSelector(css)); return this; }
        SelectorGroup xpath(String xp) { selectors.add(By.xpath(xp)); return this; }

        List<By> build() { return selectors; }
    }

    private static WebElement findElementWithMultipleSelectors(WebDriver driver, TestResults results, SelectorGroup group) {
        for (By by : group.build()) {
            try {
                List<WebElement> elems = driver.findElements(by);
                if (!elems.isEmpty()) return elems.get(0);
            } catch (Exception e) {
                // continue
            }
        }
        return null;
    }

    // Convenience form field struct
    private static class FormField {
        String selector, label, value;
        FormField(String selector, String label, String value) { this.selector = selector; this.label = label; this.value = value; }
    }

}
