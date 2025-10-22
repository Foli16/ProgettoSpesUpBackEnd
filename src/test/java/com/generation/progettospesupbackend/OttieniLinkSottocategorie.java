package com.generation.progettospesupbackend;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
public class OttieniLinkSottocategorie
{
    @Test
    void creaFileSottocategorie() throws FileNotFoundException {
        WebDriver driver = inizializeWebDriver();

        List<String[]> supermercati = ottieniSupermercatiConLocationDaFile("supermercati.txt");
        List<String[]> categorie = ottieniCategorie("categorie.txt");


        List<String> categoriesLinks = new ArrayList<>();
        for(String[] sup: supermercati)
            for(String[] cat: categorie)
            {
                String urlDaScrapare = obtainUrl(sup,cat);
//                System.out.println(urlDaScrapare);
                categoriesLinks.add(urlDaScrapare);
            }


        StringBuilder sb = new StringBuilder();

        try
        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));


            for(String linkConIntestazione: categoriesLinks)
            {
                String[] splittato = linkConIntestazione.split(";");
                String link = splittato[1];

                driver.get(link);
                Thread.sleep(1500);
                //                                                                                  TODO Vedi tutti se in italiano
                List<WebElement> linksSottocategorie = driver.findElements(By.cssSelector("a[aria-label='Vedi tutto']"));


                for(WebElement tagA :linksSottocategorie)
                {
                    String href = tagA.getAttribute("href");
                    String[] splittatoPerSlash = href.split("/");
                    String linkCompleto = linkConIntestazione+"/"+splittatoPerSlash[splittatoPerSlash.length-1];
                    sb.append(linkCompleto);
                    sb.append("\n");
                }
            }

            FileWriter fileWriter = new FileWriter("tuttilink.txt");

            fileWriter.write(sb.toString().trim());
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private  WebDriver inizializeWebDriver() {
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


    private String obtainUrl(String[] sup,String[] cat)
    {
        String intestazione=sup[0]+"-"+cat[0];
        String baseUrl="https://it.everli.com/s#/locations/[location]/stores/[store]/categories/[category]";
        baseUrl=baseUrl.replace("[location]",sup[1]).replace("[store]",sup[2]).replace("[category]",cat[1]);
        return intestazione+";"+baseUrl;
    }

    private List<String[]> ottieniSupermercatiConLocationDaFile(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        List<String[]> res = new ArrayList<>();
        while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            String[] parts = line.split(":");
            String supermercatiLocation = parts[1];
            String[] splittati = supermercatiLocation.split("-");
            String[] arr = new String[3];
            arr[0] = parts[0];
            arr[1] = splittati[0];
            arr[2] = splittati[1];
            res.add(arr);
        }

        return res;
    }

    private List<String[]> ottieniCategorie(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        List<String[]> res = new ArrayList<>();
        while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            String[] parts = line.split(":");
           res.add(parts);
        }

        return res;
    }
}
