package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.entities.Category;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.Product;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.repositories.PriceTrendRepository;
import com.generation.progettospesupbackend.model.repositories.ProductRepository;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Scraper
{
	private final SupermarketRepository supRepo;
	private final PriceTrendRepository ptRepo;
	private final ProductRepository prodRepo;
	private WebDriver driver;
	public Scraper(SupermarketRepository supRepo, PriceTrendRepository ptRepo, ProductRepository prodRepo)
	{
		this.supRepo = supRepo;
		this.ptRepo = ptRepo;
		this.prodRepo = prodRepo;

	}

	private Supermarket supermercatoCorrente;
	private EnumMap<NomiLinks,String> valoriLink;
	private Map<Product, PriceTrend> productsToPriceTrend;

	/**
	 * Questo metodo prende in ingresso una lista di links che va a scrapare
	 * @param links
	 */
	public void runScraping(List<String> links)
	{
		this.driver=generateDriver();
		for(String link : links)
		{
			fillMapProductsPriceTrends(link);
			save();
		}
	}


	private WebDriver generateDriver()
	{
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
		try
		{
			Path p = Paths.get(userDataDir);
			if (!Files.exists(p))
			{
				Files.createDirectories(p);
			}
		} catch (Exception e)
		{
			System.err.println("Impossibile creare la cartella user-data-dir: " + e.getMessage());
			// proseguiamo comunque: Chrome potrebbe crearla da solo
		}

		ChromeOptions options = new ChromeOptions();
		options.addArguments("user-data-dir=" + userDataDir);
		if (profileDirectory != null && !profileDirectory.isBlank())
		{
			options.addArguments("profile-directory=" + profileDirectory);
		}

		// opzioni utili
		options.addArguments("--no-first-run");
		options.addArguments("--no-default-browser-check");
		// NON usare --headless se vuoi riutilizzare estensioni/sessioni in modo affidabile
		// options.addArguments("--headless=new"); // non consigliato qui

		WebDriver driver = new ChromeDriver(options);
		return driver;
	}

	
	private List<WebElement> extractElements(String link)
	{

		setCurrentLinkTree(link);
		setCurrentSupermarket();
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

		return driver.findElements(By.cssSelector(".vader-product"));
	}

	/**
	 * Questo metodo riempie una mappa chiave Prodotto e valore PriceTrend estraendo i dati dal sito
	 * @param link
	 */
	private void fillMapProductsPriceTrends( String link)
	{
		productsToPriceTrend = new HashMap<>();
		for(WebElement el : extractElements(link))
		{
			Product p = extractProduct(el);
			PriceTrend pt = extractPriceTrend(el);
			productsToPriceTrend.put(p,pt);
		}
	}

	/**
	 * Metodo che estrae le informazioni sui prezzi dei prodotti dal sito e le assegna a un nuovo PriceTrend
	 * @param element È il WebElement (in questo caso quello etichettato dalla classe .vader-product) che contiene tutte le informazioni su un singolo prodotto
	 * @return
	 */
	private PriceTrend extractPriceTrend(WebElement element)
	{
		PriceTrend pr = new PriceTrend();
		pr.convertAndSetPrice(safeGetText(element, By.cssSelector(".price .vader-dynamic-string div")));
		pr.convertAndSetOriginalPrice(safeGetText(element, By.cssSelector(".full-price .vader-dynamic-string div")));
		pr.setPricePerType(safeGetText(element, By.cssSelector(".price-per-type")));
		return pr;
	}

	/**
	 * Metodo che estrae le informazioni sui prodotti dal sito e le assegna a un nuovo Product
	 * @param element È il WebElement (in questo caso quello etichettato dalla classe .vader-product) che contiene tutte le informazioni su un singolo prodotto
	 * @return
	 */
	private Product extractProduct(WebElement element)
	{
		Product p = new Product();
		p.setImgUrl(safeGetSrcText(element, By.cssSelector("img")));
		p.setName(safeGetText(element, By.cssSelector(".name")));
		p.setDescription(safeGetText(element, By.cssSelector(".description")));
		p.setCategory(Category.getByUrl(valoriLink.get(NomiLinks.CATEGORY)));
		return p;
	}


	private void save()
	{
		for(Product p : productsToPriceTrend.keySet() )
		{
			PriceTrend pt = productsToPriceTrend.get(p);
			Product prod = prodRepo.existsByImgUrlAndName(p.getImgUrl(),p.getName()) 	?
						   prodRepo.findByImgUrlAndName(p.getImgUrl(),p.getName() )	:
						   prodRepo.save(p)							;

			//occhio perchè non è detto che vada sempre creato un price trend, dovreste confrontare i prezzi
			//per vedere se sono cambiati e nel caso aggiornare quello vecchio
			pt.setProduct(prod);
			pt.setSupermarket(supermercatoCorrente);
			ptRepo.save(pt);
		}
	}


	private String safeGetText(WebElement parent, By selector)
	{
		try
		{
			WebElement el = parent.findElement(selector);
			return el.getText().trim();
		} catch (Exception e)
		{
			return null; // o null o un placeholder
		}
	}

	private String safeGetSrcText(WebElement parent, By selector)
	{
		try
		{
			return parent.findElement(selector).getAttribute("src");
		} catch (Exception e)
		{
			return null; // o null o un placeholder
		}
	}

	private void setCurrentLinkTree(String link)
	{
		valoriLink = new EnumMap<>(NomiLinks.class);
		String[] splittato = link.split("/");
		int lastIndex = splittato.length-1;
		valoriLink.put(NomiLinks.CATEGORY,splittato[lastIndex-1]);
		valoriLink.put(NomiLinks.SUB_CATEGORY,splittato[lastIndex]);
		valoriLink.put(NomiLinks.STORE,splittato[lastIndex-3]);
		valoriLink.put(NomiLinks.LOCATION,splittato[lastIndex-5]);
	}

	private void setCurrentSupermarket()
	{
		supermercatoCorrente = supRepo.findSupermarketByStoreUrl(valoriLink.get(NomiLinks.STORE));
	}

	private enum NomiLinks
	{
		CATEGORY,
		SUB_CATEGORY,
		STORE,
		LOCATION;
	}
}
