package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.entities.Category;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class LinkSottocategorieService
{
    @Autowired
    SupermarketRepository repo;
    public List<String> leggiSottocategorie() throws FileNotFoundException {
        WebDriver driver = initializeWebDriver();
        List<Supermarket> supermarkets = repo.findAll();

        List<String> categoriesLinks = new ArrayList<>();
        for(Supermarket sup: supermarkets)
            for(Category c: Category.values())
            {
                String[] locationEStore = new String[2];
                locationEStore[0] = sup.getLocationUrl();
                locationEStore[1] = sup.getStoreUrl();
                String urlDaScrapare = obtainUrl(locationEStore,c.getUrlCategoria());
                categoriesLinks.add(urlDaScrapare);
            }

        List<String> linkCompleti = new ArrayList<>();

        try
        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            for(String link: categoriesLinks)
            {
                driver.get(link);
                Thread.sleep(1500);

                List<WebElement> linksSottocategorie = driver.findElements(By.cssSelector("a[aria-label='Vedi tutto']"));


                for(WebElement tagA :linksSottocategorie)
                {
                    String href = tagA.getAttribute("href");
                    String[] splittatoPerSlash = href.split("/");
//                    Supermarket sup = repo.findSupermarketByStoreUrl(splittatoPerSlash[splittatoPerSlash.length-4]);
//                    sup.getSubcategoryUrls().add(splittatoPerSlash[splittatoPerSlash.length-1]);
                    String linkCompleto = link+"/"+splittatoPerSlash[splittatoPerSlash.length-1];
                    linkCompleti.add(linkCompleto);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return linkCompleti;
    }

    private  WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", "C:/tools/chromedriver.exe");
        String userDataDir = "C:/ChromeProfiles/EverliProfile";
        String profileDirectory = "Default"; // se non vuoi specificare, imposta a null o ""
        try {
            Path p = Paths.get(userDataDir);
            if (!Files.exists(p)) {
                Files.createDirectories(p);
            }
        } catch (Exception e) {
            System.err.println("Impossibile creare la cartella user-data-dir: " + e.getMessage());
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + userDataDir);
        if (profileDirectory != null && !profileDirectory.isBlank()) {
            options.addArguments("profile-directory=" + profileDirectory);
        }

        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    private String obtainUrl(String[] locationEStore,String cat)
    {
        //String intestazione=sup[0]+"-"+cat;
        String baseUrl="https://it.everli.com/s#/locations/[location]/stores/[store]/categories/[category]";
        baseUrl=baseUrl.replace("[location]",locationEStore[0]).replace("[store]",locationEStore[1]).replace("[category]",cat);
        return baseUrl;
    }
}
