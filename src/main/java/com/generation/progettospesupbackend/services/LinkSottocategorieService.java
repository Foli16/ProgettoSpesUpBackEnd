package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.entities.Category;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
// Rimosso import per WebDriverWait
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// Rimosso import per Duration
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // Import per le Stream API (standard Java 8+)

@Service
public class LinkSottocategorieService {

    @Autowired
    SupermarketRepository repo;

    public List<String> scrapeLinkSottocategorie() {
        WebDriver driver = initializeWebDriver();
        List<Supermarket> supermarkets = repo.findAll();

        // 1. Codice più pulito ed efficiente (Java 8+) per creare la lista di URL
        // Evita cicli annidati e la creazione di array temporanei.
        List<String> categoriesLinks = supermarkets.stream()
                .flatMap(sup -> Arrays.stream(Category.values())
                        .map(c -> obtainUrl(sup.getLocationUrl(), sup.getStoreUrl(), c.getUrlCategoria()))
                )
                .collect(Collectors.toList());

        List<String> linkCompleti = new ArrayList<>();

        // 2. Blocco try-finally ESTERNO per garantire la chiusura del driver
        // Questo assicura che driver.quit() venga chiamato anche se il loop fallisce.
        try {
            // Rimosso WebDriverWait

            for (String link : categoriesLinks) {
                // 3. Blocco try-catch INTERNO per la robustezza ("meno prono ad errori")
                // Se lo scraping di un link fallisce, logga l'errore e continua
                // con il link successivo, invece di bloccare l'intero processo.
                try {
                    driver.get(link);

                    // 4. Mantenuto Thread.sleep() come richiesto
                    Thread.sleep(1500);

                    List<WebElement> linksSottocategorie = driver.findElements(By.cssSelector("a[aria-label='Vedi tutto']"));

                    for (WebElement tagA : linksSottocategorie) {
                        String href = tagA.getAttribute("href");

                        // 5. Aggiunti controlli di robustezza per evitare NullPointerException
                        if (href != null && !href.isBlank()) {
                            String[] splittatoPerSlash = href.split("/");

                            if (splittatoPerSlash.length > 0) {
                                String linkCompleto = link + "/" + splittatoPerSlash[splittatoPerSlash.length - 1];
                                linkCompleti.add(linkCompleto);
                            } else {
                                System.err.println("WARN: Trovato href ma lo split è vuoto: " + href);
                            }
                        } else {
                            System.err.println("WARN: Trovato tag 'Vedi tutto' ma href è nullo/vuoto su: " + link);
                        }
                    }
                } catch (InterruptedException ie) {
                    // 6. Buona pratica gestire l'InterruptedException
                    System.err.println("ERRORE: Thread.sleep interrotto. " + ie.getMessage());
                    Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
                } catch (Exception e) {
                    // 7. Stampa l'errore specifico e l'URL che ha fallito, poi continua
                    System.err.println("ERRORE: Fallito scraping per l'URL: " + link);
                    e.printStackTrace(System.err); // Logga l'errore su stderr
                    // Il loop continuerà con il prossimo link
                }
            }
        } finally {
            // 8. Il blocco finally garantisce che il driver si chiuda sempre
            if (driver != null) {
                driver.quit();
            }
        }
        return linkCompleti;
    }

    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", "C:/tools/chromedriver.exe");
        String userDataDir = "C:/ChromeProfiles/EverliProfile";
        String profileDirectory = "Default";
        try {
            Path p = Paths.get(userDataDir);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (Exception e) {
            // 9. Usa System.err per coerenza (visto che non vuoi logger)
            System.err.println("Impossibile creare la cartella user-data-dir: " + e.getMessage());
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + userDataDir);

        if (profileDirectory != null && !profileDirectory.isBlank()) {
            options.addArguments("profile-directory=" + profileDirectory);
        }

        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        // 10. Aggiunte opzioni di stabilità (non sono librerie, solo flag di Chrome)
        // Utili per evitare crash in esecuzioni automatiche
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage"); // Fondamentale per server Linux/Docker
        options.addArguments("--remote-allow-origins=*"); // Spesso richiesto da versioni recenti

        return new ChromeDriver(options);
    }

    // 11. Firma del metodo semplificata, non serve un array
    private String obtainUrl(String location, String store, String cat) {
        String baseUrl = "https://it.everli.com/s#/locations/[location]/stores/[store]/categories/[category]";

        // 12. Concatenare i replace è pulito e performante
        return baseUrl.replace("[location]", location)
                .replace("[store]", store)
                .replace("[category]", cat);
    }
}