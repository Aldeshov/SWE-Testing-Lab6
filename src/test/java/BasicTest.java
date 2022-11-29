import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

import static junit.framework.TestCase.*;

public class BasicTest extends TestHelper {

    private String username = "admin";
    private String password = "admin";

    @Test
    public void titleExistsTest() {
        String expectedTitle = "ST Online Store";
        String actualTitle = driver.getTitle();

        assertEquals(expectedTitle, actualTitle);
    }

    /*
    In class Exercise
    Fill in loginLogoutTest() and login method in TestHelper, so that the test passes correctly.
    */
    @Test
    public void loginLogoutTest() {

        login(username, password);

        // assert that correct page appeared
        WebElement adminHeader = driver.findElement(By.linkText("Products"));
        assertEquals(adminHeader.getText(), "Products");

        logout();
    }

    /*
    In class Exercise
    Write a test case, where you make sure, that one can’t log in with a false password
    */
    @Test
    public void loginFalsePasswordTest() {
        username = "wrong-username";
        password = "wrong-password";
        login(username, password);

        // assert that notice is displayed
        WebElement noticeText = driver.findElement(By.id("notice"));
        assertEquals(noticeText.getText(), "Invalid user/password combination"); // Wrong password text
    }

    @Test
    public void registerNewUserTest() {
        String newName = "user";
        String newPassword = "password";

        driver.get(baseUrlAdmin);

        goToPage("Register");

        driver.findElement(By.id("user_name")).sendKeys(newName);
        driver.findElement(By.id("user_password")).sendKeys(newPassword);
        driver.findElement(By.id("user_password_confirmation")).sendKeys(newPassword);

        By createUserButtonXpath = By.xpath("//input[@value='Create User']");
        WebElement register = driver.findElement(createUserButtonXpath);
        register.click();

        deleteLoggedInUserTest();
    }

    @Test
    public void deleteLoggedInUserTest() {
        goToPage("Admin");
        driver.findElement(By.linkText("Delete")).click();
        WebElement noticeText = driver.findElement(By.id("notice"));
        assertEquals(noticeText.getText(), "User was successfully deleted."); // Deleted user text
    }

    @Test
    public void deleteLoggedInLastUserTest() {
        driver.get(baseUrlAdmin);
        goToPage("Admin");
        driver.findElement(By.linkText("Delete")).click();
        WebElement noticeText = driver.findElement(By.id("notice"));
        assertEquals(noticeText.getText(), "Can't delete last user"); // Last user not deleted user text
    }

    @Test
    public void adminDeleteLoggedInLastUserTest() {
        login(username, password);
        deleteLoggedInLastUserTest();
    }

    @Test
    public void hasAtLeastOneProductTest() {
        login(username, password);
        goToPage("Products");
        List<WebElement> elementDescriptions = driver.findElements(By.className("list_description"));
        assertTrue("products size", elementDescriptions.size() > 0);
    }

    @Test
    public void isLoggedInTest() {
        driver.get(baseUrlAdmin);
        assertTrue(isElementPresent(By.linkText("Logout")));
    }

    public void productEditPageFromList(String productId) {
        isLoggedInTest();
        goToPage("Products");
        assertTrue(isElementPresent(By.id(productId))); // Make sure product exists

        WebElement product = driver.findElement(By.id(productId));
        WebElement editLink = product.findElement(By.linkText("Edit"));
        editLink.click();
    }

    // Returns previous value
    public String setInputValue(String id, String value) {
        isElementPresent(By.id(id));
        WebElement field = driver.findElement(By.id(id));
        String previousValue = field.getAttribute("value");
        for (int i = 0; i < previousValue.length(); i++) {
            field.sendKeys(Keys.BACK_SPACE);
        }
        field.sendKeys(value);
        return previousValue;
    }

    // Returns previous value
    public String setSelectOptionsValue(String id, String value) {
        isElementPresent(By.id(id));
        WebElement field = driver.findElement(By.id(id));
        String previousValue = field.getAttribute("value");
        field.click();
        field.findElement(By.xpath("//option[@value='" + value + "']")).click();
        return previousValue;
    }


    @Test
    public void updateProductTest() {
        assertTrue(driver.findElements(By.className("product_header")).size() > 0);

        By updateProductButtonXpath = By.xpath("//input[@value='Update Product']");
        WebElement update = driver.findElement(updateProductButtonXpath);
        update.click();

        WebElement noticeText = driver.findElement(By.id("notice"));
        assertEquals(noticeText.getText(), "Product was successfully updated."); // Last user not deleted user text
    }

    @Test
    public void updateAndRevertChangesProductTest() {
        login(username, password);
        productEditPageFromList("Updated product title");

        String productTitle = setInputValue("product_title", "updated title");
        String productDescription = setInputValue("product_description", "updated description");
        String productType = setSelectOptionsValue("product_prod_type", "Sunglasses");
        String productPrice = setInputValue("product_price", "321");

        updateProductTest();

        driver.findElement(By.linkText("Edit")).click();

        setInputValue("product_title", productTitle);
        setInputValue("product_description", productDescription);
        setSelectOptionsValue("product_prod_type", productType);
        setInputValue("product_price", productPrice);

        updateProductTest();
    }

    @Test
    public void updateWrongPriceTest() {
        login(username, password);
        productEditPageFromList("Updated product title");

        setInputValue("product_price", "text");

        driver.findElement(By.xpath("//input[@value='Update Product']")).click();

        WebElement errorText = driver.findElement(By.id("error_explanation"));
        List<WebElement> errors = errorText.findElements(By.tagName("h2"));
        assertTrue(errors.size() > 0);
        assertEquals(errors.get(0).getText(), "1 error prohibited this product from being saved:");
    }

    @Test
    public void newProductTest() {
        login(username, password);
        goToPage("Products");
        driver.findElement(By.linkText("New product")).click();
        setInputValue("product_title", "NEW Product");
        setInputValue("product_description", "NEW Product desc");
        setSelectOptionsValue("product_prod_type", "Other");
        setInputValue("product_price", "1234");

        driver.findElement(By.xpath("//input[@value='Create Product']")).click();

        assertTrue(isElementPresent(By.id("NEW Product")));
        deleteProduct("NEW Product");
    }

    public void deleteProduct(String id) {
        isElementPresent(By.id(id));
        driver.findElement(By.id(id)).findElement(By.linkText("Delete")).click();
        WebElement noticeText = driver.findElement(By.id("notice"));
        assertTrue(isElementPresent(By.id("notice")));
        assertEquals(noticeText.getText(), "Product was successfully destroyed.");
    }

    @Test
    public void deleteProductTest() {
        login(username, password);
        goToPage("Products");
        deleteProduct("NEW Product");
    }

    /* End-user ux testing */

    @Test
    public void checkOnlySunglassesTest() {
        goToPage("Sunglasses");
        isElementPresent(By.id("Sunglasses"));

        List<WebElement> products = driver.findElements(By.className("entry"));
        for (WebElement product : products) {
            assertTrue(product.findElement(By.id("category")).getText().contains("Sunglasses"));
        }
    }

    @Test
    public void checkOnlyBooksTest() {
        goToPage("Books");
        isElementPresent(By.id("Books"));

        List<WebElement> products = driver.findElements(By.className("entry"));
        for (WebElement product : products) {
            assertTrue(product.findElement(By.id("category")).getText().contains("Books"));
        }
    }

    @Test
    public void checkOnlyOthersTest() {
        goToPage("Other");
        isElementPresent(By.id("Other"));

        List<WebElement> products = driver.findElements(By.className("entry"));
        for (WebElement product : products) {
            assertTrue(product.findElement(By.id("category")).getText().contains("Other"));
        }
    }

    @Test
    public void searchProductsTest() {
        goToPage("Home");
        isElementPresent(By.id("search_input"));
        setInputValue("search_input", "some");

        List<WebElement> products = driver.findElements(By.className("entry"));
        for (WebElement product : products) {
            if (product.isDisplayed()) {
                List<WebElement> headers = product.findElements(By.tagName("h3"));
                assertTrue(headers.size() > 0);
                List<WebElement> links = headers.get(0).findElements(By.tagName("a"));
                assertTrue(links.size() > 0);
                assertTrue(links.get(0).getText().toLowerCase().contains("some"));
            }
        }
    }

    @Test
    public void checkAddToCartTest() {
        goToPage("Home");

        List<WebElement> products = driver.findElements(By.className("entry"));
        ArrayList<WebElement> selectedProducts = new ArrayList<WebElement>();
        new WebDriverWait(driver, waitForResposeTime)
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfAllElements(products));
        for (WebElement product : products) {
            if (getRandomBoolean()) {
                product.findElement(By.xpath(".//input[@value='Add to Cart']")).click();
                selectedProducts.add(product);
                List<WebElement> headers = product.findElements(By.tagName("h3"));
                assertTrue(headers.size() > 0);
                List<WebElement> links = headers.get(0).findElements(By.tagName("a"));
                assertTrue(links.size() > 0);

                new WebDriverWait(driver, waitForResposeTime).ignoring(
                        StaleElementReferenceException.class).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("current_item"))
                );
            }
        }

        List<WebElement> rows = driver.findElements(By.className("cart_row"));
        assertEquals(selectedProducts.size(), rows.size());

        goToPage("Books");

        products = driver.findElements(By.className("entry"));
        new WebDriverWait(driver, waitForResposeTime)
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfAllElements(products));
        for (WebElement product : products) {
            if (getRandomBoolean()) {
                product.findElement(By.xpath(".//input[@value='Add to Cart']")).click();
                selectedProducts.add(product);
                List<WebElement> headers = product.findElements(By.tagName("h3"));
                assertTrue(headers.size() > 0);
                List<WebElement> links = headers.get(0).findElements(By.tagName("a"));
                assertTrue(links.size() > 0);

                new WebDriverWait(driver, waitForResposeTime).ignoring(
                        StaleElementReferenceException.class).until(
                        ExpectedConditions.presenceOfElementLocated(By.id("current_item"))
                );
            }
        }

        rows = driver.findElements(By.className("cart_row"));
        assertEquals(selectedProducts.size(), rows.size());

        emptyCartTest();
    }

    public void addAndWaitProduct(WebElement product) {
        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.visibilityOf(product)
        );

        boolean alreadyAdded = isElementPresent(By.id("current_item"));
        List<WebElement> headers = product.findElements(By.tagName("h3"));
        assertTrue(headers.size() > 0);
        List<WebElement> links = headers.get(0).findElements(By.tagName("a"));
        assertTrue(links.size() > 0);
        String productName = links.get(0).getText();

        if (alreadyAdded) {
            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("current_item"))
            );

            WebElement row = driver.findElement(By.id("current_item"));
            List<WebElement> elements = row.findElements(By.xpath("./child::*"));
            String rowCountString = elements.get(0).getText();
            int rowCount = Integer.parseInt(rowCountString.substring(0, rowCountString.length() - 1));

            product.findElement(By.xpath(".//input[@value='Add to Cart']")).click();

            System.out.println(productName);
            System.out.println(driver.findElement(By.id("current_item")).findElements(By.xpath("./child::*")).get(1).getText());

            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.textToBePresentInElement(driver.findElement(By.id("current_item")).findElements(By.xpath("./child::*")).get(1), productName)
            );

            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.textToBePresentInElement(
                            driver.findElement(By.id("current_item")).findElements(By.xpath("./child::*")).get(0), (++rowCount) + "x"
                    )
            );
        } else {
            product.findElement(By.xpath(".//input[@value='Add to Cart']")).click();

            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("current_item"))
            );
        }
    }

    @Test
    public void emptyCartTest() {
        isElementPresent(By.xpath("//input[@value='Empty cart']"));
        WebElement emptyButton = driver.findElement(By.xpath("//input[@value='Empty cart']"));
        emptyButton.click();

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.textToBe(By.id("notice"), "Cart successfully deleted.")
        );

        WebElement notice = driver.findElement(By.id("notice"));
        assertEquals("Cart successfully deleted.", notice.getText());
    }

    @Test
    public void purchaseWithAllItemAddOrDeleteVariantsTest() {
//        checkAddToCartTest(); // Check add and clear;
//        goToPage("Home");
        List<WebElement> products = driver.findElements(By.className("entry"));
        new WebDriverWait(driver, waitForResposeTime)
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfAllElements(products));
        for (WebElement product : products) {
            for (int i = 0; i < 3; i++) {
                if (getRandomBoolean()) {
                    addAndWaitProduct(product);
                }
            }
        }

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("cart_row"))
        );
        List<WebElement> rows = driver.findElements(By.className("cart_row"));
        double total = 0;
        for (WebElement row: rows) {
            List<WebElement> data = row.findElements(By.xpath("./child::*"));
            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.visibilityOfAllElements(data)
            );
            int count = Integer.parseInt(data.get(0).getText().substring(0, data.get(0).getText().length() - 1));

            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.presenceOfElementLocated(By.className("item_price"))
            );
            double price = Double.parseDouble(row.findElement(By.className("item_price")).getText().substring(1).replace(",", ""));
            total += (price * count);
        }
        double totalElement = Double.parseDouble(driver.findElement(By.className("total_cell")).getText().substring(1).replace(",", ""));
        assertEquals(total, totalElement);
    }

    @Test
    public void purchaseTest() {
        List<WebElement> products = driver.findElements(By.className("entry"));
        new WebDriverWait(driver, waitForResposeTime)
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfAllElements(products));
        products.get(0).findElement(By.xpath(".//input[@value='Add to Cart']")).click();

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.presenceOfElementLocated(By.id("current_item"))
        );

        products.get(0).findElement(By.xpath(".//input[@value='Add to Cart']")).click();

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.stalenessOf(driver.findElement(By.id("current_item")).findElements(By.xpath("./child::*")).get(0))
        );

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("cart_row"))
        );
        List<WebElement> rows = driver.findElements(By.className("cart_row"));
        double total = 0;
        for (WebElement row: rows) {
            List<WebElement> data = row.findElements(By.xpath("./child::*"));
            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.visibilityOfAllElements(data)
            );
            int count = Integer.parseInt(data.get(0).getText().substring(0, data.get(0).getText().length() - 1));

            new WebDriverWait(driver, waitForResposeTime).ignoring(
                    StaleElementReferenceException.class).until(
                    ExpectedConditions.presenceOfElementLocated(By.className("item_price"))
            );
            double price = Double.parseDouble(products.get(0).findElement(By.className("price")).getText().substring(1).replace(",", ""));
            total += (price * count);
        }
        double totalElement = Double.parseDouble(driver.findElement(By.className("total_cell")).getText().substring(1).replace(",", ""));
        assertEquals(total, totalElement);

        driver.findElement(By.xpath(".//input[@value='Checkout']")).click();

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.presenceOfElementLocated(By.id("order_page"))
        );

        setInputValue("order_name", "Test Order name");
        setInputValue("order_address", "Test Order address");
        setInputValue("order_email", "Test Order email");
        setSelectOptionsValue("order_pay_type", "Credit card");

        driver.findElement(By.xpath(".//input[@value='Place Order']")).click();

        new WebDriverWait(driver, waitForResposeTime).ignoring(
                StaleElementReferenceException.class).until(
                ExpectedConditions.presenceOfElementLocated(By.id("order_receipt"))
        );
        double result = Double.parseDouble(driver.findElement(By.className("total_cell")).getText().substring(1).replace(",", ""));
        assertEquals(total, result);
    }
}
