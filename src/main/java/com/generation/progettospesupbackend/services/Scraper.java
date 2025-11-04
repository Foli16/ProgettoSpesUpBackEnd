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
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class Scraper
{
	private final SupermarketRepository supRepo;
	private final PriceTrendRepository ptRepo;
	private final ProductRepository prodRepo;
	private WebDriver driver;

	// --- MODIFICA ---
	// Numero massimo di tentativi per link
	private static final int MAX_RETRIES = 3;

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
	 * Questo metodo prende in ingresso una lista di links che va a scrapare.
	 * Implementa una logica di retry per i link che falliscono.
	 * @param links
	 */
	public void runScraping(List<String> links)
	{
		// --- MODIFICA --- Logica di retry
		List<String> linksToProcess = new ArrayList<>(links);
		Map<String, Integer> retryCounts = new HashMap<>();

		try
		{
			this.driver = generateDriver();

			while (!linksToProcess.isEmpty())
			{
				String link = linksToProcess.remove(0); // Prende e rimuove il primo link
				int currentRetries = retryCounts.getOrDefault(link, 0);

				try
				{
					System.out.println("Inizio scraping per (tentativo " + (currentRetries + 1) + "/" + MAX_RETRIES + "): " + link);

					// Esegue l'intero processo per un link: scrape + save
					fillMapProductsPriceTrends(link);
					save(); // Questo metodo ora è "batchizzato"

					System.out.println("Salvataggio completato per: " + link);

				} catch (Exception e)
				{
					System.err.println("Errore durante l'elaborazione del link: " + link);
					e.printStackTrace();

					if (currentRetries < MAX_RETRIES - 1)
					{
						retryCounts.put(link, currentRetries + 1);
						linksToProcess.add(link); // Aggiunge il link fallito in fondo alla coda per un nuovo tentativo
						System.err.println("Link " + link + " accodato per un nuovo tentativo.");
					} else
					{
						System.err.println("Link " + link + " ha fallito dopo " + MAX_RETRIES + " tentativi. Si rinuncia.");
					}
				}
			}
			// --- FINE MODIFICA ---

		} catch (Exception e) {
			System.err.println("Errore fatale nell'inizializzazione dello Scraper: " + e.getMessage());
		}
	}


	private WebDriver generateDriver()
	{
		// path al chromedriver
		System.setProperty("webdriver.chrome.driver", "C:/tools/chromedriver.exe");

		// -------------------------------------------------------
		// CONFIGURA QUI sotto il profilo che vuoi usare
		// Esempi:
		//  userDataDir = "C:/ChromeProfiles/EverliProfile"
		//  oppure usare il profilo Chrome reale:
		//  userDataDir = "C:/Users/tuoUser/AppData/Local/Google/Chrome/User Data"
		//  profileDirectory = "Default" oppure "Profile 1"
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
				Thread.sleep(5000);
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
			Product p = extractProduct(el, link);
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
	private Product extractProduct(WebElement element, String link)
	{
		Product p = new Product();
		p.setImgUrl(safeGetSrcText(element, By.cssSelector("img")));
		p.setName(safeGetText(element, By.cssSelector(".name")));
		p.setDescription(safeGetText(element, By.cssSelector(".description")));
		p.setCategory(Category.getByUrl(valoriLink.get(NomiLinks.CATEGORY)));
		return p;
	}


	/**
	 * // --- MODIFICA ---
	 * Metodo di salvataggio "batchizzato".
	 * Salva i nuovi prodotti singolarmente (necessario per ottenere l'ID).
	 * Raccoglie tutti i PriceTrend (nuovi e da aggiornare) e li salva
	 * in due chiamate batch (saveAll) per efficienza.
	 */
	private void save()
	{
		List<PriceTrend> trendsToSave = new ArrayList<>();   // Batch list per nuovi PT
		List<PriceTrend> trendsToUpdate = new ArrayList<>(); // Batch list per vecchi PT da chiudere

		for(Map.Entry<Product, PriceTrend> entry : productsToPriceTrend.entrySet())
		{
			Product p = entry.getKey();
			PriceTrend pt = entry.getValue();

			// Ottimizzazione: usa findBy... invece di exists... + findBy...
			Product prodottoEsistente = prodRepo.findByImgUrlAndName(p.getImgUrl(), p.getName());

			if(prodottoEsistente != null)
			{
				// Prodotto Esistente: la logica può essere batchizzata
				PriceTrend oldPt = prodottoEsistente.getActivePrice(supermercatoCorrente);

				if(oldPt == null)
				{
					// Nessun prezzo attivo, aggiungi quello nuovo alla lista batch
					saveNewPriceTrend(prodottoEsistente, pt, trendsToSave); // Helper modificato
					// --- BUG FIX --- Rimosso il 'return' prematuro
				}
				else if(!Objects.equals(oldPt.getPrice(), pt.getPrice()))
				{
					// Il prezzo è cambiato: chiudi il vecchio, aggiungi il nuovo
					oldPt.setEndDate(LocalDate.now());
					trendsToUpdate.add(oldPt); // Aggiungi alla lista batch di aggiornamento

					pt.setStartDate(LocalDate.now());
					pt.setProduct(prodottoEsistente);
					pt.setSupermarket(supermercatoCorrente);
					trendsToSave.add(pt); // Aggiungi alla lista batch di salvataggio
				}
				// else: prezzo identico, non fare nulla
			}
			else
			{
				// Prodotto Nuovo: non può essere batchizzato facilmente
				// a causa della dipendenza dall'ID. Usiamo il metodo helper originale.
				saveNewPriceTrend(p, pt);
			}
		}

		// --- MODIFICA --- Esegui le operazioni batch
		if (!trendsToUpdate.isEmpty()) {
			System.out.println("Aggiornamento di " + trendsToUpdate.size() + " PriceTrend (chiusura).");
			ptRepo.saveAll(trendsToUpdate);
		}
		if (!trendsToSave.isEmpty()) {
			System.out.println("Salvataggio di " + trendsToSave.size() + " nuovi PriceTrend.");
			ptRepo.saveAll(trendsToSave);
		}
	}

	/**
	 * Helper per salvare un *nuovo* prodotto e il suo primo PriceTrend.
	 * Questo non è batchizzato perché pt dipende dall'ID di p.
	 */
	private void saveNewPriceTrend(Product p, PriceTrend pt)
	{
		System.out.println("Salvataggio nuovo prodotto: " + p.getName());
		p = prodRepo.save(p); // Salva e ottiene l'ID

		pt.setStartDate(LocalDate.now());
		pt.setProduct(p);
		pt.setSupermarket(supermercatoCorrente);
		ptRepo.save(pt); // Salva il PT associato
	}

	/**
	 * // --- NUOVO METODO HELPER (Overload) ---
	 * Helper per aggiungere un PriceTrend a un prodotto *esistente*
	 * alla lista batch, senza salvarlo immediatamente.
	 */
	private void saveNewPriceTrend(Product prodottoEsistente, PriceTrend pt, List<PriceTrend> trendsToSave)
	{
		pt.setStartDate(LocalDate.now());
		pt.setProduct(prodottoEsistente);
		pt.setSupermarket(supermercatoCorrente);
		trendsToSave.add(pt); // Aggiunge alla lista per il batch
	}


	private String safeGetText(WebElement parent, By selector)
	{
		try
		{
			WebElement el = parent.findElement(selector);
			String text = el.getText();
			if (text == null || text.isEmpty()) {
				return null;
			}
			// 1. Normalizza l'Unicode (es. 'e' + '´' diventa 'é')
			//    Questo risolve i problemi di accenti "strani"
			text = Normalizer.normalize(text, Normalizer.Form.NFC);
			// 2. Sostituisce QUALSIASI carattere di spaziatura (incluso \u00A0)
			//    con un singolo spazio normale, poi fa il trim.
			//    Questo risolve gli spazi "strani".
			text = text.replaceAll("\\s+", " ").trim();
			return text;

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