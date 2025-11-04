package com.generation.progettospesupbackend;

import com.generation.progettospesupbackend.model.entities.LinkCategorie;
import com.generation.progettospesupbackend.model.repositories.LinkCategorieRepo;
import com.generation.progettospesupbackend.services.LinkSottocategorieService;
import com.generation.progettospesupbackend.services.Scraper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
public class TestScraperVStefano
{
	@Autowired
	RunScraping scrap;

	@Test
	void run()
	{
		scrap.run();
	}



}
