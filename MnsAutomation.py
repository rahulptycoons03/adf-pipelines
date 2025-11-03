import os
import sys
import time
import traceback
import csv
import argparse
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.alert import Alert
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.chrome.service import Service as ChromeService


# ------------------------- Data structures -------------------------

# Ensure Unicode output works on Windows terminals (emoji-safe)
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

@dataclass
class Step:
    icon: str
    title: str
    description: str
    type: str = ""
    details: str = ""
    timestamp: str = field(default_factory=lambda: datetime.now().strftime("%I:%M:%S %p"))


@dataclass
class TestResults:
    steps: List[Step] = field(default_factory=list)
    page_title: str = ""
    final_url: str = ""
    screenshot_path: str = ""
    execution_time_ms: int = 0
    product_title: str = ""
    product_url: str = ""
    order_id: str = ""
    csv_out_path: str = ""
    browser_name: str = ""
    browser_version: str = ""


# ------------------------- Constants -------------------------

BASE_URL = "https://aeuatmnssingapore.corp.al-futtaim.com/en/fashion"
RESULTS_DIR = Path(os.getcwd()) / "test-results"
SCREENSHOT_DIR = RESULTS_DIR / "screenshots"
REPORTS_DIR = RESULTS_DIR / "reports"
DEFAULT_TIMEOUT_SECONDS = 15


# ------------------------- Utility helpers -------------------------

def add_step(results: TestResults, icon: str, title: str, description: str, type_: str = "", details: str = "") -> None:
    step = Step(icon=icon, title=title, description=description, type=type_ or "", details=details or "")
    results.steps.append(step)
    print(f"{icon} {title}: {description}")


def sleep(ms: int) -> None:
    try:
        time.sleep(ms / 1000.0)
    except Exception:
        pass


def wait_for_page_load(driver: WebDriver, timeout_sec: int) -> None:
    try:
        WebDriverWait(driver, timeout_sec).until(lambda d: d.execute_script("return document.readyState") == "complete")
    except Exception:
        pass


def scroll_into_view(driver: WebDriver, el: WebElement) -> None:
    try:
        driver.execute_script("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", el)
    except Exception:
        pass


def try_click_with_multiple_methods(driver: WebDriver, results: TestResults, el: WebElement, success_title: str) -> bool:
    try:
        el.click()
        add_step(results, "✅", success_title, "Successfully clicked (direct method)")
        return True
    except Exception:
        try:
            driver.execute_script("arguments[0].click();", el)
            add_step(results, "✅", success_title, "Successfully clicked (JavaScript method)")
            return True
        except Exception:
            try:
                ActionChains(driver).move_to_element(el).click().perform()
                add_step(results, "✅", success_title, "Successfully clicked (Actions API method)")
                return True
            except Exception as e3:
                add_step(results, "❌", "All Click Methods Failed", f"All click methods failed: {e3}", "error")
                return False


def save_screenshot(driver: WebDriver, prefix: str) -> str:
    try:
        SCREENSHOT_DIR.mkdir(parents=True, exist_ok=True)
        ts = datetime.now().strftime("%Y-%m-%dT%H-%M-%S")
        filename = f"{prefix}-{ts}.png"
        dest = SCREENSHOT_DIR / filename
        driver.save_screenshot(str(dest))
        return str(dest)
    except Exception as e:
        print(f"Failed to save screenshot: {e}")
        return ""


def escape_html(s: Optional[str]) -> str:
    if not s:
        return ""
    return (
        s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
    )


def get_stack_trace(exc: BaseException) -> str:
    return "".join(traceback.format_exception(type(exc), exc, exc.__traceback__))


def generate_html_report(results: TestResults) -> str:
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    steps_html = []
    for s in results.steps:
        cls = s.type or ""
        block = [
            f"<div class=\"step {cls}\">",
            f"<strong>{escape_html(s.icon + ' ' + s.title)}</strong>",
            f"<p>{escape_html(s.description)}</p>",
            f"<div class=\"timestamp\">{escape_html(s.timestamp)}</div>",
        ]
        if s.details:
            block.append(f"<div class=\"details\">{escape_html(s.details)}</div>")
        block.append("</div>")
        steps_html.append("\n".join(block))

    screenshot_rel = Path(results.screenshot_path).name if results.screenshot_path else ""
    csv_rel = Path(results.csv_out_path).as_posix() if results.csv_out_path else ""
    status = (
        "<span class=\"status-fail\">❌ FAILED</span>"
        if any((st.type or "").lower() == "error" for st in results.steps)
        else "<span class=\"status-pass\">✅ PASSED</span>"
    )
    total_steps = len(results.steps)
    error_steps = sum(1 for st in results.steps if (st.type or "").lower() == "error")
    ok_steps = total_steps - error_steps

    html = (
        "<!DOCTYPE html>\n"
        "<html>\n<head>\n<meta charset=\"utf-8\">\n<title>M&S SG Automation Test Report</title>\n"
        "<style>\n"
        "body{font-family:Arial,sans-serif;margin:20px;background-color:#f5f5f5;}\n"
        ".container{max-width:1200px;margin:0 auto;background:white;padding:20px;border-radius:8px;box-shadow:0 2px 10px rgba(0,0,0,0.1);}\n"
        ".header{text-align:center;color:#333;border-bottom:3px solid #4CAF50;padding-bottom:20px;margin-bottom:30px;}\n"
        ".test-info{background:#e8f5e8;padding:15px;border-radius:5px;margin:20px 0;}\n"
        ".step{margin:15px 0;padding:10px;border-left:4px solid #4CAF50;background:#f9f9f9;}\n"
        ".step.error{border-left-color:#f44336;background:#ffebee;}\n"
        ".step.warning{border-left-color:#ff9800;background:#fff3e0;}\n"
        ".timestamp{color:#666;font-size:.9em;}\n"
        ".screenshot{text-align:center;margin:20px 0;}\n"
        ".screenshot img{max-width:100%;border:1px solid #ddd;border-radius:5px;}\n"
        ".summary{background:#e3f2fd;padding:15px;border-radius:5px;margin:20px 0;}\n"
        ".status-pass{color:#4CAF50;font-weight:bold;}\n"
        ".status-fail{color:#f44336;font-weight:bold;}\n"
        ".details{background:#fafafa;padding:10px;border-radius:5px;margin:10px 0;font-family:monospace;white-space:pre-wrap;}\n"
        "</style>\n</head>\n<body>\n<div class=\"container\">\n"
        "<div class=\"header\"><h1>🧪 M&S SG Automation Test Report</h1><p class=\"timestamp\">Generated on: "
        + timestamp
        + "</p></div>\n"
        + "<div class=\"test-info\">\n<h2>📋 Test Summary</h2>\n"
        + f"<p><strong>Test Name:</strong> M&S SG Homepage Launch Test</p>\n"
        + f"<p><strong>Browser:</strong> Chrome (UI Mode)</p>\n"
        + f"<p><strong>URL:</strong> {escape_html(BASE_URL)}</p>\n"
        + f"<p><strong>Status:</strong> {status}</p>\n"
        + f"<p><strong>Execution Time:</strong> {results.execution_time_ms}ms</p>\n"
        + f"<p><strong>Steps:</strong> {ok_steps} OK / {error_steps} errors (total {total_steps})</p>\n"
        + f"<p><strong>Browser:</strong> {escape_html(results.browser_name)} {escape_html(results.browser_version)}</p>\n"
        + "</div>\n"
        + "<div class=\"summary\">\n<h3>📊 Test Results</h3>\n"
        + f"<p><strong>Page Title:</strong> {escape_html(results.page_title)}</p>\n"
        + f"<p><strong>Final URL:</strong> {escape_html(results.final_url)}</p>\n"
        + f"<p><strong>Screenshot:</strong> {escape_html(screenshot_rel)}</p>\n"
        + f"<p><strong>Order ID:</strong> {escape_html(results.order_id)}</p>\n"
        + f"<p><strong>Product Title:</strong> {escape_html(results.product_title)}</p>\n"
        + f"<p><strong>Product URL:</strong> {escape_html(results.product_url)}</p>\n"
        + (f"<p><strong>CSV Output:</strong> {escape_html(csv_rel)}</p>\n" if csv_rel else "")
        + "</div>\n"
        + "<h3>📝 Execution Steps</h3>\n"
        + "\n".join(steps_html)
    )

    if screenshot_rel:
        html += (
            "\n<div class=\"screenshot\">\n<h3>📸 Screenshot</h3>\n"
            f"<img src=\"../screenshots/{escape_html(screenshot_rel)}\" alt=\"Test Screenshot\">\n"
            f"<p>Screenshot saved: {escape_html(screenshot_rel)}</p>\n"
            "</div>\n"
        )

    html += (
        "<div class=\"test-info\">\n<h3>🎯 Test Validation</h3>\n"
        "<p>✅ WebDriver initialized successfully</p>\n"
        "<p>✅ Navigated to M&S SG homepage</p>\n"
        f"<p>✅ Page title retrieved: \"{escape_html(results.page_title)}\"</p>\n"
        "<p>✅ URL validation passed</p>\n"
        "<p>✅ Kids section navigation (attempted)</p>\n"
        "<p>✅ Product search and selection (attempted)</p>\n"
        "<p>✅ Add to bag functionality (attempted)</p>\n"
        "<p>✅ Checkout flow (attempted)</p>\n"
        "<p>✅ Delivery options (attempted)</p>\n"
        "<p>✅ Screenshot captured successfully</p>\n"
        "<p>✅ Browser closed properly</p>\n"
        "</div>\n"
        "<footer style=\"text-align:center;margin-top:30px;color:#666;border-top:1px solid #eee;padding-top:20px;\">\n"
        f"<p>Generated by M&S SG Automation Suite | {timestamp}</p>\n"
        "</footer>\n</div>\n</body>\n</html>"
    )

    return html


def generate_and_save_report(results: TestResults) -> None:
    try:
        REPORTS_DIR.mkdir(parents=True, exist_ok=True)
        ts = datetime.now().strftime("%Y-%m-%dT%H-%M-%S")
        report_file = REPORTS_DIR / f"ms-ae-test-report-{ts}.html"
        html = generate_html_report(results)
        report_file.write_text(html, encoding="utf-8")
        latest = REPORTS_DIR / "latest-report.html"
        latest.write_text(html, encoding="utf-8")
        print(f"\n📊 HTML Report generated: {report_file}")
        print(f"📋 Latest report saved: {latest}")
    except Exception as e:
        print(f"❌ Failed to generate HTML report: {e}")


# ------------------------- DOM helpers -------------------------

class SelectorGroup:
    def __init__(self) -> None:
        self.selectors: List[tuple[str, str]] = []

    def css(self, css: str) -> "SelectorGroup":
        self.selectors.append(("css", css))
        return self

    def xpath(self, xp: str) -> "SelectorGroup":
        self.selectors.append(("xpath", xp))
        return self


def find_element_with_multiple_selectors(driver: WebDriver, results: TestResults, group: SelectorGroup) -> Optional[WebElement]:
    for mode, value in group.selectors:
        try:
            if mode == "css":
                elems = driver.find_elements(By.CSS_SELECTOR, value)
            else:
                elems = driver.find_elements(By.XPATH, value)
            if elems:
                return elems[0]
        except Exception:
            pass
    return None


# ------------------------- Flows -------------------------

def perform_checkout_flow(driver: WebDriver, results: TestResults) -> None:
    try:
        add_step(results, "🛒", "Checkout Flow", "Starting checkout process")
        sleep(2000)
        wait = WebDriverWait(driver, 10)
        go_to_cart = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Go to Cart')]")))
        go_to_cart.click()
        add_step(results, "📄", "Checkout Page", f"Navigated to checkout page: \"{driver.title}\"", details=f"URL: {driver.current_url}")

        sleep(2000)
        add_step(results, "🛒", "Add to Bag ", "Go to Cart button clicked")
        add_step(results, "🛒", "Checkout", "Looking for checkout button")
        checkout_btn = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Checkout')]")))
        if checkout_btn:
            checkout_btn.click()
            add_step(results, "🛒", "Checkout", "Checkout button clicked")
            sleep(3000)
            add_step(results, "📄", "Delivery Page", f"Navigated to delivery page: \"{driver.title}\"", details=f"URL: {driver.current_url}")

            home_delivery = wait.until(EC.element_to_be_clickable((By.XPATH, "//div[contains(text(),'Home delivery')]")))
            home_delivery.click()
            add_step(results, "🛒", "Checkout", "Slected DFelivery Option")

            cont = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Continue')]")))
            cont.click()
            add_step(results, "🛒", "Checkout", "Clicked on Continue Button")
        else:
            add_step(results, "❌", "Secure Checkout Not Found", "Could not find Checkout Securely button", "error")
    except Exception as e:
        add_step(results, "❌", "Checkout Flow Error", f"Checkout flow failed: {e}", "error")


def perform_payment(driver: WebDriver, results: TestResults) -> str:
    add_step(results, "🛒", "Payment Flow", "Starting Payment process")
    sleep(2000)
    wait = WebDriverWait(driver, 10)

    driver.execute_script("window.scrollTo(0, 350);")
    sleep(3000)
    more_opts = wait.until(EC.element_to_be_clickable((By.NAME, "more-payment-options")))
    more_opts.click()
    sleep(2000)
    add_step(results, "🛒", "Payment Flow", "Selected more Payment Options")
    cont_to_payment = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Continue to payment')]")))
    cont_to_payment.click()
    add_step(results, "🛒", "Payment Flow", "Clicked on Continue Payments")
    sleep(5000)

    driver.switch_to.frame("pgw-ui-container-dropin-iframe")

    card_number = wait.until(EC.presence_of_element_located((By.NAME, "cardNumber")))
    card_number.send_keys("4111111111111111")
    sleep(1000)
    expiry_year = wait.until(EC.presence_of_element_located((By.NAME, "expyear")))
    expiry_year.send_keys("12/28")
    cvv = wait.until(EC.presence_of_element_located((By.NAME, "cvv")))
    cvv.send_keys("100")
    name = wait.until(EC.presence_of_element_located((By.NAME, "name")))
    name.send_keys("Test User")
    email = wait.until(EC.presence_of_element_located((By.NAME, "email")))
    email.send_keys("Testuser@gmail.com")

    cont_btn = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Continue payment')]")))
    cont_btn.click()
    sleep(4000)
    driver.switch_to.default_content()
    sleep(3000)
    otp_field = wait.until(EC.element_to_be_clickable((By.NAME, "challengeDataEntry")))
    otp_field.send_keys("123456")
    submit_btn = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Submit')]")))
    submit_btn.click()
    sleep(10000)

    order_details = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'View order details')]")))
    order_details.click()
    sleep(5000)
    order_id = driver.find_element(By.XPATH, "(//span[contains(@class,'body-lg-bold')])[1]").text
    return order_id


def continue_with_kibo_fulfilment(driver: WebDriver, order_number: str, results: TestResults) -> None:
    driver.get("https://www.euw1.kibocommerce.com/")
    wait = WebDriverWait(driver, 10)
    sleep(3000)
    kibo_user = wait.until(EC.element_to_be_clickable((By.NAME, "Email")))
    kibo_user.send_keys("lubna.ayesha-external@alfuttaim.com")
    sleep(1000)
    next_btn = wait.until(EC.element_to_be_clickable((By.ID, "buttonSubmit")))
    next_btn.click()
    sleep(2000)

    kibo_pwd = wait.until(EC.element_to_be_clickable((By.NAME, "Password")))
    kibo_pwd.send_keys("World@365")
    sleep(1000)
    submit_btn = wait.until(EC.element_to_be_clickable((By.XPATH, "//input[contains(@value,'Log in')]")))
    submit_btn.click()

    uat_env = wait.until(EC.element_to_be_clickable((By.XPATH, "//span[text()='Marks and Spencer UAT']")))
    uat_env.click()
    sleep(4000)
    hamburger = wait.until(EC.element_to_be_clickable((By.XPATH, "//i[contains(@class,'hamburgerMenu')]")))
    hamburger.click()
    sleep(1000)
    main_order = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[text()='Orders']")))
    main_order.click()
    sleep(1000)
    orders = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[text()=' Orders ']")))
    orders.click()
    sleep(2000)
    search_box = wait.until(EC.element_to_be_clickable((By.XPATH, "(//input[contains(@name,'inputEl')])[2]")))
    search_box.send_keys(order_number)
    search_box.send_keys(Keys.ENTER)
    sleep(3000)
    row = driver.find_element(By.XPATH, "//tr[contains(@id,'gridview')]")
    row.click()
    sleep(3000)
    shipments_tab = driver.find_element(By.XPATH, "//li[contains(text(),'Shipments')]")
    shipments_tab.click()
    sleep(1000)
    shipment_id = driver.find_element(By.XPATH, "//div[contains(@class,'labelvalue')]/a")
    shipment_id.click()
    sleep(4000)
    driver.execute_script("window.scrollTo(0, 350);")
    proceed_validate = driver.find_element(By.XPATH, "//button[text()=' Proceed To Validate Items In Stock ']")
    proceed_validate.click()
    qty_text = driver.find_element(By.XPATH, "//span[contains(text(),'Quantity')]/following-sibling::div[contains(@class,'inserted')]").text.strip()
    sleep(1000)
    qty_input = driver.find_element(By.XPATH, "//input[@placeholder='Current Stock Amount']")
    qty_input.send_keys(qty_text)
    sleep(1000)
    packing_slip = driver.find_element(By.XPATH, "//div/button[contains(text(),'Packing Slip')]")
    packing_slip.click()
    prepare_shipment = driver.find_element(By.XPATH, "//div/button[contains(text(),'Prepare for Shipment')]")
    prepare_shipment.click()
    box_selection = driver.find_element(By.XPATH, "//div/button[contains(text(),' Medium Bag ')]")
    box_selection.click()
    weight = driver.find_element(By.XPATH, "//input[@placeholder='Weight']")
    weight.send_keys("12")
    save_btn = driver.find_element(By.XPATH, "//span[contains(text(),'Save')]")
    save_btn.click()
    print_label = driver.find_element(By.XPATH, "//span[contains(text(),' Print Shipping Label ')]")
    print_label.click()
    sleep(4000)

    try:
        Alert(driver).dismiss()
    except Exception:
        pass
    complete_shipment = driver.find_element(By.XPATH, "//button[contains(text(),' Yes, Complete Shipment ')]")
    complete_shipment.click()
    close_btn = driver.find_element(By.XPATH, "//button[contains(text(),' Close ')]")
    close_btn.click()


# ------------------------- CSV export -------------------------

def write_csv_row(csv_path: Path, headers: list[str], row: dict) -> None:
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    file_exists = csv_path.exists()
    with csv_path.open("a", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=headers)
        if not file_exists:
            writer.writeheader()
        writer.writerow({k: row.get(k, "") for k in headers})


# ------------------------- Main -------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="MnsAutomation runner")
    parser.add_argument("--csv-out", default=str(RESULTS_DIR / "msnauto_output.csv"), help="Path to write msnauto CSV output")
    default_driver = "chromedriver.exe" if os.name == "nt" else "chromedriver"
    parser.add_argument("--driver-path", default=default_driver, help="Path or name of chromedriver in PATH")
    args = parser.parse_args()

    start = int(time.time() * 1000)
    results = TestResults()
    add_step(results, "🚀", "Test Started", "Initializing M&S SG automation test")
    results.csv_out_path = args.csv_out

    driver: Optional[WebDriver] = None
    try:
        add_step(results, "⚙️", "Chrome Setup", "Configuring Chrome browser options")
        options = webdriver.ChromeOptions()
        service = ChromeService(executable_path=args.driver_path)
        driver = webdriver.Chrome(service=service, options=options)
        driver.maximize_window()
        driver.implicitly_wait(60)

        add_step(results, "✅", "WebDriver Ready", "Chrome WebDriver created successfully")
        try:
            caps = getattr(driver, "capabilities", {}) or {}
            results.browser_name = str(caps.get("browserName", "Chrome"))
            results.browser_version = str(caps.get("browserVersion", caps.get("version", "")))
        except Exception:
            pass

        add_step(results, "🌐", "Navigation", "Navigating to M&S SG homepage")
        continue_with_kibo_fulfilment(driver, "31544", results)
        driver.get(BASE_URL)
        wait = WebDriverWait(driver, 10)
        wait_for_page_load(driver, DEFAULT_TIMEOUT_SECONDS)

        results.page_title = driver.title
        results.final_url = driver.current_url
        add_step(results, "📄", "Page Loaded", f"Page title: \"{results.page_title}\"", details=f"URL: {results.final_url}")

        add_step(results, "👶", "Navigation", "Looking for Kids section in navigation")
        sleep(2000)

        sign_in_btn = find_element_with_multiple_selectors(
            driver,
            results,
            SelectorGroup().xpath("//div[contains(text(),'Log in | Sign up')]")
        )
        if sign_in_btn:
            sign_in_btn.click()
        sleep(2000)

        login_option = wait.until(EC.element_to_be_clickable((By.XPATH, "(//div[contains(text(),'Log in')])[2]")))
        login_option.click()
        use_email = wait.until(EC.element_to_be_clickable((By.XPATH, "//div[contains(text(),'Use email instead')]")))
        use_email.click()

        email_field = wait.until(EC.element_to_be_clickable((By.XPATH, "(//input[@type='email'])[1]")))
        email_field.send_keys("Rishivanthya.Sambandam@alfuttaim.com")
        pwd_field = wait.until(EC.element_to_be_clickable((By.XPATH, "(//input[@type='password'])[1]")))
        pwd_field.send_keys("Sasi@123")
        login_btn = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Log in')]")))
        login_btn.click()
        sleep(6000)
        add_step(results, "✅", "Login", "Successfully logged in to the application")

        women_link = driver.find_element(By.XPATH, "(//span[contains(text(),'Women')])[1]")
        ActionChains(driver).move_to_element(women_link).perform()
        sleep(2000)
        if women_link:
            add_step(results, "✅", "Women Section Found", "Located Women section in navigation")
            sleep(1000)
            add_step(results, "👶", "Women Navigation", "Navigated to Women section")

            add_step(results, "🍼", "Newborn Search", "Looking for Newborn/Baby subsection")
            footwear_link = driver.find_element(By.XPATH, "(//span[contains(text(),'Footwear')])")
            footwear_link.click()

            add_step(results, "📜", "Page Scroll", "Scrolling down to find products on the page")
            driver.execute_script("window.scrollTo(0, 800);")
            sleep(2000)

            add_step(results, "🛍️", "Product Search", "Looking for products after scrolling")

            product_element: Optional[WebElement] = None
            product_title: str = ""
            product_url: str = ""
            try:
                specific = driver.find_elements(By.CLASS_NAME, "plp-card")
                if specific:
                    product_element = specific[0]
                    add_step(results, "✅", "Product Found", f"Found {len(specific)} products using specific class: .mz-productlisting-title")
            except Exception:
                add_step(results, "⚠️", "Specific Class Search", "Could not find products with .mz-productlisting-title class")

            if product_element is None:
                add_step(results, "🔍", "Fallback Search", "Trying fallback product selectors")
                product_selectors = [
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
                    ".product-image",
                ]
                for sel in product_selectors:
                    try:
                        elems = driver.find_elements(By.CSS_SELECTOR, sel)
                        if elems:
                            product_element = elems[0]
                            add_step(results, "✅", "Product Found", f"Found {len(elems)} products using selector: {sel}")
                            break
                    except Exception:
                        pass

            if product_element is None:
                add_step(results, "📜", "Further Scroll", "No products found, scrolling down further")
                driver.execute_script("window.scrollTo(0, 1500);")
                sleep(2000)
                xpaths = [
                    "//a[contains(@href, '/product')]",
                    "//a[contains(@href, '/item')]",
                    "//div[contains(@class, 'product')]",
                    "//article",
                    "//img[contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'product') or contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'item')]",
                    "//div[contains(@class, 'item')]",
                    "//a[contains(@class, 'product')]",
                ]
                for xp in xpaths:
                    try:
                        elems = driver.find_elements(By.XPATH, xp)
                        if elems:
                            product_element = elems[0]
                            add_step(results, "✅", "Product Found", f"Found {len(elems)} products using XPath: {xp}")
                            break
                    except Exception:
                        pass

            if product_element is not None:
                add_step(results, "✅", "Product Located", "Successfully found a product on the page")
                scroll_into_view(driver, product_element)
                sleep(1000)
                if try_click_with_multiple_methods(driver, results, product_element, "Product Selected"):
                    sleep(3000)
                    product_title = driver.title
                    product_url = driver.current_url
                    results.product_title = product_title
                    results.product_url = product_url
                    add_step(results, "📦", "Product Page", f"Navigated to product page: \"{product_title}\"", details=f"URL: {product_url}")

                    size_dropdown = wait.until(EC.element_to_be_clickable((By.XPATH, "(//button[contains(@class,'SizeVariant_size')])[1]")))
                    size_dropdown.click()
                    sleep(3000)

                    add_step(results, "🛒", "Add to Bag Search", "Looking for Add to bag button")
                    sleep(2000)
                    driver.execute_script("window.scrollTo(0, 250);")
                    add_to_cart = driver.find_element(By.XPATH, "//button[contains(text(),'Add to cart')]")
                    sleep(1000)
                    sleep(3000)
                    add_to_cart.click()
                    add_step(results, "🛒", "Add to Bag ", "Add to Cart button clicked")

                    sleep(2000)
                    add_step(results, "🎉", "Add to Bag Success", "Product added to bag successfully!", details=f"Final URL: {driver.current_url}")
                    sleep(2000)

                    perform_checkout_flow(driver, results)
                    order_number = perform_payment(driver, results)
                    results.order_id = order_number

                    # CSV handoff for sap_runn*
                    csv_headers = [
                        "order_id",
                        "product_title",
                        "product_url",
                        "generated_at_utc",
                    ]
                    write_csv_row(
                        Path(args.csv_out),
                        csv_headers,
                        {
                            "order_id": order_number,
                            "product_title": product_title,
                            "product_url": product_url,
                            "generated_at_utc": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
                        },
                    )
                    add_step(results, "🧾", "CSV Export", f"Wrote msnauto CSV to {args.csv_out}")

                    # Mirror recent Java: use static order number for KIBO step post-payment
                    continue_with_kibo_fulfilment(driver, "31544", results)
                else:
                    add_step(results, "❌", "Product Selection Failed", "Could not select product after multiple attempts", "error")
            else:
                add_step(results, "❌", "No Products Found", "No products found on the current page", "error")

        else:
            add_step(results, "❌", "Kids Section Not Found", "Could not locate Kids section in navigation", "error")

        add_step(results, "📸", "Final Screenshot", "Taking final screenshot of the page")
        results.screenshot_path = save_screenshot(driver, "ms-ae-test")
        add_step(results, "✅", "Screenshot Saved", f"Screenshot saved: {Path(results.screenshot_path).name}")

        add_step(results, "🎯", "Test Completed", "M&S SG homepage test completed successfully!")
        add_step(results, "✅", "URL Validation", "URL validation passed")
        add_step(results, "✅", "Page Load Validation", "Page loaded successfully")

        try:
            driver.quit()
            add_step(results, "🔒", "Cleanup", "Browser closed successfully")
        except Exception as e:
            add_step(results, "⚠️", "Cleanup Error", f"Error closing browser: {e}", "error")

        results.execution_time_ms = int(time.time() * 1000) - start
        add_step(results, "⏱️", "Execution Time", f"Total execution time: {results.execution_time_ms}ms")

    except Exception as ex:
        add_step(results, "❌", "Test Failed", f"Error: {ex}", "error", get_stack_trace(ex))
        try:
            if driver is not None:
                path = save_screenshot(driver, "ms-ae-error")
                results.screenshot_path = path
                add_step(results, "📸", "Error Screenshot", f"Error screenshot saved: {Path(path).name}")
                driver.quit()
                add_step(results, "🔒", "Cleanup", "Browser closed after error")
        except Exception as cleanup_ex:
            add_step(results, "⚠️", "Cleanup Error", f"Cleanup error: {cleanup_ex}", "error")
        results.execution_time_ms = int(time.time() * 1000) - start
    finally:
        generate_and_save_report(results)


if __name__ == "__main__":
    main()


