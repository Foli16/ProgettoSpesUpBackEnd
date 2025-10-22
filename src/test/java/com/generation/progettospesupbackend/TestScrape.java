package com.generation.progettospesupbackend;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@SpringBootTest
public class TestScrape
{

    @Test
    void provaConProfiloChrome() {
        // path al chromedriver
        System.setProperty("webdriver.chrome.driver", "C:/tools/chromedriver.exe");

        // -------------------------------------------------------
        // CONFIGURA QUI sotto il profilo che vuoi usare
        // Esempi:
        //  userDataDir = "C:/ChromeProfiles/EverliProfile"
        //  oppure usare il profilo Chrome reale:
        //  userDataDir = "C:/Users/tuoUser/AppData/Local/Google/Chrome/User Data"
        //  profileDirectory = "Default" oppure "Profile 1"
        // -------------------------------------------------------
        String userDataDir = "C:/ChromeProfiles/EverliProfile";
        String profileDirectory = "Default"; // se non vuoi specificare, imposta a null o ""

        // crea la cartella se non esiste (utile la prima volta)
        try {
            Path p = Paths.get(userDataDir);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (Exception e) {
            System.err.println("Impossibile creare la cartella user-data-dir: " + e.getMessage());
            // proseguiamo comunque: Chrome potrebbe crearla da solo
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + userDataDir);
        if (profileDirectory != null && !profileDirectory.isBlank()) {
            options.addArguments("profile-directory=" + profileDirectory);
        }

        // opzioni utili
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        // NON usare --headless se vuoi riutilizzare estensioni/sessioni in modo affidabile
        // options.addArguments("--headless=new"); // non consigliato qui

        WebDriver driver = new ChromeDriver(options);

        try(BufferedReader reader = new BufferedReader(new FileReader("tuttilink.txt")))
        {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            // vai direttamente alla pagina prodotti (assumendo il profilo è già loggato)
            driver.get("https://it.everli.com/s#/locations/12923/stores/9550/categories/3/100113");

            // scrolla fino in fondo per caricare tutti i prodotti (infinite scroll)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            while (true) {
                // Scroll in fondo alla pagina
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Attendi un po' per permettere il caricamento dei nuovi prodotti
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Controlla se l'altezza della pagina è cambiata
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                if (newHeight == lastHeight) {
                    break; // siamo in fondo
                }

                lastHeight = newHeight;
            }

            // aspetta che gli elementi dei prodotti siano visibili
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product-data")));

            List<WebElement> products = driver.findElements(By.cssSelector(".product-data"));


            for (WebElement product : products) {
                String name = safeGetText(product, By.cssSelector(".name"));
                String descr = safeGetText(product, By.cssSelector(".description"));
                System.out.println(name + " - " + descr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Chiudi il browser alla fine del test:
            // Se vuoi tenere la sessione aperta per debug, commenta la riga seguente.
            driver.quit();
        }
    }

    /**
     * Metodo helper per leggere testo in modo più robusto (evita NoSuchElementException).
     */
    private String safeGetText(WebElement parent, By selector) {
        try {
            WebElement el = parent.findElement(selector);
            return el.getText().trim();
        } catch (Exception e) {
            return ""; // o null o un placeholder
        }
    }
}
