package com.rso.microservice.service;


import com.google.gson.Gson;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.rso.microservice.entity.*;
import com.rso.microservice.util.MDCUtil;
import com.rso.prices.Comparison;
import com.rso.prices.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class PricesService {
	private static final Logger log = LoggerFactory.getLogger(PricesService.class);

	final ProductService productService;
	final ShopService shopService;
	final ProductTypeService productTypeService;
	final ProductShopService productShopService;
	final ProductShopHistoryService productShopHistoryService;
	private final RestTemplate restTemplate;

	@Value("${prices.api.url}")
	private String pricesApiUrl;

	@Value("${prices.images.api.url}")
	private String pricesImagesApiUrl;

	public PricesService(ProductService productService, ShopService shopService,
						 ProductTypeService productTypeService, ProductShopService productShopService,
						 ProductShopHistoryService productShopHistoryService, RestTemplate restTemplate) {
		this.productService = productService;
		this.shopService = shopService;
		this.productTypeService = productTypeService;
		this.productShopService = productShopService;
		this.productShopHistoryService = productShopHistoryService;
		this.restTemplate = restTemplate;
	}

	@Async
	public void fetchPricesAllShops(boolean alsoFetchImages, String version, String requestId) {
		fetchPricesShop(null, alsoFetchImages, version, requestId);
	}

	@Async
	public void fetchPricesShop(Long idShop, boolean alsoFetchImages, String version, String requestId) {
		MDCUtil.putAll("Prices", version, requestId);
		String result = callNasaSuperHrana(version, requestId);

		Gson gson = new Gson();
		Comparison comparison = gson.fromJson(result, Comparison.class);

		Pattern patternKg = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?kg");
		Pattern patternG = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?g");
		Pattern patternL = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?l");
		for (com.rso.prices.Product productApi : comparison.getProducts()) {
			log.info("processing {}", productApi.getNovoIme());

			if (idShop != null && checkIfProductHasPriceOfThisShop(idShop, productApi)) {
				log.info("product has no price for specified shop, continuing to next product");
				continue;
			}

			Product product = new Product();
			product.setName(productApi.getNovoIme());
			product.setBrand(productApi.getBlagovnaZnamka());

			if (alsoFetchImages) {
				byte[] productImage = callNasaSuperHranaImage(productApi.getId(), version, requestId);
				if (productImage != null)
					product.setImage(productImage);
			}

			String enota = productApi.getEnota();
			Matcher matcherKg = patternKg.matcher(enota);
			Matcher matcherG = patternG.matcher(enota);
			Matcher matcherL = patternL.matcher(enota);
			if (matcherKg.find()) {
				BigDecimal concentration = BigDecimal.valueOf(
						Double.parseDouble(
								matcherKg.group().substring(enota.indexOf("/") + 1, enota.indexOf("kg"))));

				product.setConcentration(concentration);
				product.setConcentrationUnit(ConcentrationUnitEnum.KILOGRAM);
			} else if (matcherG.find()) {
				BigDecimal concentration = BigDecimal.valueOf(Double.parseDouble(
						matcherG.group().substring(enota.indexOf("/") + 1, enota.indexOf("g"))) / 1000);

				product.setConcentration(concentration);
				product.setConcentrationUnit(ConcentrationUnitEnum.KILOGRAM);
			} else if (matcherL.find()) {
				BigDecimal concentration = BigDecimal.valueOf(
						Double.parseDouble(
								matcherL.group().substring(enota.indexOf("/") + 1, enota.indexOf("l"))));

				product.setConcentration(concentration);
				product.setConcentrationUnit(ConcentrationUnitEnum.LITER);
			} else {
				continue;
			}

			ProductType productType = new ProductType();
			productType.setName(productApi.getKategorija());
			product.setProductType(productTypeService.createOrUpdateType(productType));

			product = productService.createOrUpdateProduct(product);

			for (Price priceApi : productApi.getPrices()) {
				Shop shop = new Shop();
				shop.setName(priceApi.getTrgovina());
				shop = shopService.createOrUpdateShop(shop);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy");
				// atStartOfDay() because LocalDateTime.parse will fail without time
				LocalDateTime date = LocalDate.parse(priceApi.getDate(), formatter).atStartOfDay();
				if (priceApi.getCenaKosarica() != null) {
					BigDecimal cenaKosarice = BigDecimal.valueOf(
							Double.parseDouble(priceApi.getCenaKosarica()));
					ProductShop productShop = productShopService.findProductShop(shop, product);
					if (productShop != null) {
						ProductShopHistory productShopHistory = productShopHistoryService.findProductShopHistory(date,
								productShop);
						if (productShopHistory == null) {
							productShopHistory = new ProductShopHistory();
							productShopHistory.setDate(date);
							productShopHistory.setPriceEUR(cenaKosarice);
							productShopHistory.setProductShop(productShop);
							productShopHistory = productShopHistoryService.createProductShopHistory(productShopHistory);
						}
					} else {
						productShop = new ProductShop();
						productShop.setPriceEUR(cenaKosarice);
						productShop.setShop(shop);
						productShop.setProduct(product);
						productShop = productShopService.createProductShop(productShop);

						ProductShopHistory productShopHistory = new ProductShopHistory();
						productShopHistory.setDate(date);
						productShopHistory.setPriceEUR(cenaKosarice);
						productShopHistory.setProductShop(productShop);
						productShopHistory = productShopHistoryService.createProductShopHistory(productShopHistory);
					}
				}
			}
		}
	}

	@HystrixCommand(fallbackMethod = "circuitBreaker")
	public String callNasaSuperHrana(String version, String requestId) {
		MDCUtil.putAll("Prices", version, requestId);
		log.info("calling: {}", pricesApiUrl);
		ResponseEntity<String> response = restTemplate.exchange(pricesApiUrl, HttpMethod.GET, null, String.class);
		log.info("finished calling {}", pricesApiUrl);
		return response.getBody();
	}

	public String circuitBreaker(String version, String requestId) {
		MDCUtil.putAll("Prices", version, requestId);
		log.error("called circuit breaker for nasa super hrana");
		return "";
	}

	@HystrixCommand(fallbackMethod = "circuitBreaker2")
	public byte[] callNasaSuperHranaImage(Integer idProduct, String version, String requestId) {
		MDCUtil.putAll("Prices", version, requestId);
		String url = String.format("%s/a8_primerjalnik_velike-%d.jpg", pricesImagesApiUrl, idProduct);
		log.info("calling: {}", url);
		ResponseEntity<byte[]> responseImage = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
		log.info("finished calling {}", url);
		return responseImage.getBody();
	}

	public byte[] circuitBreaker2(Integer idProduct, String version, String requestId) {
		MDCUtil.putAll("Prices", version, requestId);
		log.error("called circuit breaker for nasa super hrana images");
		return null;
	}

	private boolean checkIfProductHasPriceOfThisShop(Long idShop, com.rso.prices.Product productApi) {
		log.info("check if product has price for shop with id {}", idShop);
		Optional<Shop> optionalShopRequest = shopService.findById(idShop);

		if (optionalShopRequest.isEmpty()) {
			// if shop doesn't exist, ignore and continue
			log.info("shop with id {} doesn't exists", idShop);
			return true;
		}
		Shop shopRequest = optionalShopRequest.get();

		log.info("checking if product {} has price for shop {}", productApi.getNovoIme(), shopRequest.getName());
		return productApi.getPrices().stream()
				.anyMatch(price -> price.getTrgovina().equals(shopRequest.getName()));
	}

}
