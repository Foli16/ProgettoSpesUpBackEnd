package com.generation.progettospesupbackend.model.entities;

import lombok.Getter;

@Getter
public enum Category
{
	FRUTTA_E_VERDURA("3"),
	CARNE_E_PESCE("1"),
	FORMAGGI_SALUMI_E_GASTRONOMIA("4"),
	LATTE_BURRO_E_YOGURT("6"),
	PANE_E_PASTICCERIA("8"),
	PASTA_RISO_E_CEREALI("12"),
	VEGETARIANO_E_VEGANO("32"),
	SUGHI_SCATOLAME_E_CONDIMENTI("27"),
	UOVA_FARINE_E_PREPARATI("28"),
	CAFFE_TE_E_ZUCCHERO("10"),
	COLAZIONE_DOLCIUMI_E_SNACK("11"),
	SURGELATI_E_GELATI("15"),
	ACQUA_E_BEVANDE("101366"),
	BIRRERIA_ED_ENOTECA("16"),
	CURA_PERSONA("19"),
	PULIZIA_DELLA_CASA("20"),
	CURA_CASA("21"),
	BIMBI_E_INFANZIA("18"),
	AMICI_ANIMALI("17"),
	GIARDINO_E_HOBBY("13"),
	CARTOLERIA("101656");

	private final String urlCategoria;

	Category(String url)
	{
		this.urlCategoria = url;
	}

	public static Category getByUrl(String cat)
	{
		for (Category c : Category.values())
			if (c.getUrlCategoria().equalsIgnoreCase(cat))
				return c;
		return null;
	}
}
