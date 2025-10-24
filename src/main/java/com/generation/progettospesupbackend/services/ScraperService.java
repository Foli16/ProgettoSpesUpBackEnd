package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.entities.Category;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.Product;
import com.generation.progettospesupbackend.model.repositories.PriceTrendRepository;
import com.generation.progettospesupbackend.model.repositories.ProductRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
@Service
public class ScraperService
{
    @Autowired
    private LinkSottocategorieService serv;
    @Autowired
    private ProductRepository repo;
    @Autowired
    private PriceTrendRepository prRepo;

    /**
	 * Uno scraper ideato per estrarre i dati dei prodotti dalle pagine delle sottocategorie su Everli. <br/>
	 * Pagina di esempio: <a href="https://it.everli.com/s#/locations/13647/stores/5540/categories/3/100113">https://it.everli.com/s#/locations/13647/stores/5540/categories/3/100113</a><br/>
     *
	 */
    public void scrape() {
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
        try//(BufferedReader reader = new BufferedReader(new FileReader("tuttilink.txt")))
        {
            for(String link : serv.leggiSottocategorie())
            {

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

                // vai direttamente alla pagina prodotti (assumendo il profilo è già loggato)
                driver.get(link);

                // scrolla fino in fondo per caricare tutti i prodotti (infinite scroll)
                JavascriptExecutor js = (JavascriptExecutor) driver;
                long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

                while (true)
                {
                    // Scroll in fondo alla pagina
                    js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                    // Attendi un po' per permettere il caricamento dei nuovi prodotti
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    // Controlla se l'altezza della pagina è cambiata
                    long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                    if (newHeight == lastHeight)
                    {
                        break; // siamo in fondo
                    }

                    lastHeight = newHeight;
                }

                // aspetta che gli elementi dei prodotti siano visibili
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".vader-product")));

                List<WebElement> products = driver.findElements(By.cssSelector(".vader-product"));

                salvaProdotti(link, products);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Chiudi il browser alla fine del test:
            // Se vuoi tenere la sessione aperta per debug, commenta la riga seguente.
            driver.quit();
        }
    }

    private void salvaProdotti(String link, List<WebElement> products)
    {
        for (WebElement product : products)
        {
            Product p = new Product();
            p.setImgUrl(product.findElement(By.cssSelector("img")).getAttribute("src"));
            p.setName(safeGetText(product, By.cssSelector(".name")));
            p.setDescription(safeGetText(product, By.cssSelector(".description")));
//                    https://it.everli.com/s#/locations/13647/stores/5540/categories/3/100113
            String[] cat = link.split("/");
            for(Category c : Category.values())
                if(c.getUrlCategoria().equalsIgnoreCase(cat[cat.length-2]))
                    p.setCategory(c);
            PriceTrend pr = new PriceTrend();
            pr.convertAndSetPrice(safeGetText(product, By.cssSelector(".price .vader-dynamic-string div")));
//                    1,39 €
            pr.convertAndSetOriginalPrice(safeGetText(product, By.cssSelector(".full-price .vader-dynamic-string div")));
            pr.setPricePerType(safeGetText(product, By.cssSelector(".price-per-type")));
//                    2,80 €/kg
            p.addPrice(pr);

            prRepo.save(pr);
            repo.save(p);
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
            return null; // o null o un placeholder
        }
    }
}
