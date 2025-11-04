package com.generation.progettospesupbackend;

import com.generation.progettospesupbackend.model.entities.LinkCategorie;
import com.generation.progettospesupbackend.model.repositories.LinkCategorieRepo;
import com.generation.progettospesupbackend.services.LinkSottocategorieService;
import com.generation.progettospesupbackend.services.Scraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RunScraping
{
	@Autowired
	LinkCategorieRepo linkRepo;

	@Autowired
	Scraper serv;

	@Autowired
	LinkSottocategorieService linkService;

	public void run()
	{
		List<LinkCategorie> links = linkRepo.findAll();
		List<String> tuttiLinks = links.stream().map(l->l.getValue()).toList();
		serv.runScraping(tuttiLinks);
	}

	public void obtainsLinks()
	{
		List<String> links = linkService.scrapeLinkSottocategorie();
		List<LinkCategorie> all = links.stream().map(l -> new LinkCategorie(l)).toList();
		linkRepo.saveAll(all);
	}

}
