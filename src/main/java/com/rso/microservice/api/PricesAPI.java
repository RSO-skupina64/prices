package com.rso.microservice.api;

import com.google.gson.Gson;
import com.rso.microservice.api.dto.ErrorDto;
import com.rso.microservice.api.dto.MessageDto;
import com.rso.microservice.api.dto.PricesShopRequestDto;
import com.rso.microservice.api.mapper.PricesMapper;
import com.rso.microservice.entity.*;
import com.rso.microservice.service.PricesService;
import com.rso.prices.Comparison;
import com.rso.prices.Price;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prices")
@OpenAPIDefinition(info = @Info(title = "Prices API",
        description = "This is API documentation for Prices Microservice",
        version = "0.1"))
@Tag(name = "Prices")
public class PricesAPI {

    final PricesService pricesService;

    final PricesMapper pricesMapper;

    @Autowired

    public PricesAPI(PricesService pricesService, PricesMapper pricesMapper) {
        this.pricesService = pricesService;
        this.pricesMapper = pricesMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetches prices for all shops",
            description = "Fetches prices for all shops")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prices",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<MessageDto> fetchProductPrices(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
                                                         @RequestParam(required = false, defaultValue = "false") boolean fetchPictures) {
        // todo jwt validation
        // todo move to properties
        String pricesApi = "https://www.nasasuperhrana.si/wp-admin/admin-ajax.php?action=products_data";
        String pricesImageApi = "https://www.nasasuperhrana.si/wp-content/uploads";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(pricesApi, HttpMethod.GET, null, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String result = response.getBody();

            Gson gson = new Gson();
            Comparison comparison = gson.fromJson(result, Comparison.class);

            Pattern patternKg = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?kg");
            Pattern patternG = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?g");
            Pattern patternL = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?l");
            for (com.rso.prices.Product productApi : comparison.getProducts()) {
                Product product = new Product();
                product.setName(productApi.getNovoIme());
                product.setBrand(productApi.getBlagovnaZnamka());

                if (fetchPictures) {
                    try {
                        ResponseEntity<byte[]> responseImage = restTemplate.exchange(
                                String.format("%s/a8_primerjalnik_velike-%d.jpg", pricesImageApi, productApi.getId()),
                                HttpMethod.GET, null, byte[].class);
                        if (responseImage.getStatusCode() == HttpStatus.OK) {
                            product.setImage(responseImage.getBody());
                        }
                    } catch (Exception e) {
                        // ignore all exceptions
                    }
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
                            Double.parseDouble(matcherL.group().substring(enota.indexOf("/") + 1, enota.indexOf("l"))));

                    product.setConcentration(concentration);
                    product.setConcentrationUnit(ConcentrationUnitEnum.LITER);
                } else {
                    continue;
                }

                ProductType productType = new ProductType();
                productType.setName(productApi.getKategorija());
                product.setProductType(pricesService.createOrUpdateType(productType));

                product = pricesService.createOrUpdateProduct(product);

                for (Price priceApi : productApi.getPrices()) {
                    Shop shop = new Shop();
                    shop.setName(priceApi.getTrgovina());
                    shop = pricesService.createOrUpdateShop(shop);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy");
                    // atStartOfDay() because LocalDateTime.parse will fail without time
                    LocalDateTime date = LocalDate.parse(priceApi.getDate(), formatter).atStartOfDay();
                    if (priceApi.getCenaKosarica() != null) {
                        BigDecimal cenaKosarice = BigDecimal.valueOf(Double.parseDouble(priceApi.getCenaKosarica()));
                        ProductShop productShop = pricesService.findProductShop(shop, product);
                        if (productShop != null) {
                            ProductShopHistory productShopHistory = pricesService.findProductShopHistory(date,
                                    productShop);
                            if (productShopHistory == null) {
                                productShopHistory = new ProductShopHistory();
                                productShopHistory.setDate(date);
                                productShopHistory.setPriceEUR(cenaKosarice);
                                productShopHistory.setProductShop(productShop);
                                productShopHistory = pricesService.createProductShopHistory(productShopHistory);
                            }
                        } else {
                            productShop = new ProductShop();
                            productShop.setPriceEUR(cenaKosarice);
                            productShop.setShop(shop);
                            productShop.setProduct(product);
                            productShop = pricesService.createProductShop(productShop);

                            ProductShopHistory productShopHistory = new ProductShopHistory();
                            productShopHistory.setDate(date);
                            productShopHistory.setPriceEUR(cenaKosarice);
                            productShopHistory.setProductShop(productShop);
                            productShopHistory = pricesService.createProductShopHistory(productShopHistory);
                        }
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("fetchProductPrices successful"));
    }

    @PostMapping(value = "/shop", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetches prices for specific shop",
            description = "Fetches prices for specific shop")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prices",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<MessageDto> fetchProductPricesSpecificShop(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @Valid @RequestBody PricesShopRequestDto pricesShopRequest,
            @RequestParam(required = false, defaultValue = "false") boolean fetchPictures) {
        // todo jwt validation
        // todo move to properties
        String pricesApi = "https://www.nasasuperhrana.si/wp-admin/admin-ajax.php?action=products_data";
        String pricesImageApi = "https://www.nasasuperhrana.si/wp-content/uploads";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(pricesApi, HttpMethod.GET, null, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String result = response.getBody();

            // find the shop for the request id
            Optional<Shop> optionalShopRequest = pricesService.findById(pricesShopRequest.getIdShop());
            if (optionalShopRequest.isPresent()) {
                Shop shopRequest = optionalShopRequest.get();

                Gson gson = new Gson();
                Comparison comparison = gson.fromJson(result, Comparison.class);

                Pattern patternKg = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?kg");
                Pattern patternG = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?g");
                Pattern patternL = Pattern.compile("\\/\\d+(\\.\\d+|,\\d+)?l");
                for (com.rso.prices.Product productApi : comparison.getProducts()) {
                    // filter the prices to only the ones for the selected shop
                    List<com.rso.prices.Price> filteredPricesApi = productApi.getPrices().stream()
                            .filter(price -> price.getTrgovina().equals(shopRequest.getName()))
                            .toList();

                    // if we have no prices for the selected shop, we do not want to add this product
                    if (filteredPricesApi.size() > 0) {
                        Product product = new Product();
                        product.setName(productApi.getNovoIme());
                        product.setBrand(productApi.getBlagovnaZnamka());

                        if (fetchPictures) {
                            try {
                                ResponseEntity<byte[]> responseImage = restTemplate.exchange(
                                        String.format("%s/a8_primerjalnik_velike-%d.jpg", pricesImageApi,
                                                productApi.getId()),
                                        HttpMethod.GET, null, byte[].class);
                                if (responseImage.getStatusCode() == HttpStatus.OK) {
                                    product.setImage(responseImage.getBody());
                                }
                            } catch (Exception e) {
                                // ignore all exceptions
                            }
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
                        product.setProductType(pricesService.createOrUpdateType(productType));

                        product = pricesService.createOrUpdateProduct(product);

                        for (Price priceApi : filteredPricesApi) {
                            Shop shop = new Shop();
                            shop.setName(priceApi.getTrgovina());
                            shop = pricesService.createOrUpdateShop(shop);

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy");
                            // atStartOfDay() because LocalDateTime.parse will fail without time
                            LocalDateTime date = LocalDate.parse(priceApi.getDate(), formatter).atStartOfDay();
                            if (priceApi.getCenaKosarica() != null) {
                                BigDecimal cenaKosarice = BigDecimal.valueOf(
                                        Double.parseDouble(priceApi.getCenaKosarica()));
                                ProductShop productShop = pricesService.findProductShop(shop, product);
                                if (productShop != null) {
                                    ProductShopHistory productShopHistory = pricesService.findProductShopHistory(date,
                                            productShop);
                                    if (productShopHistory == null) {
                                        productShopHistory = new ProductShopHistory();
                                        productShopHistory.setDate(date);
                                        productShopHistory.setPriceEUR(cenaKosarice);
                                        productShopHistory.setProductShop(productShop);
                                        productShopHistory = pricesService.createProductShopHistory(productShopHistory);
                                    }
                                } else {
                                    productShop = new ProductShop();
                                    productShop.setPriceEUR(cenaKosarice);
                                    productShop.setShop(shop);
                                    productShop.setProduct(product);
                                    productShop = pricesService.createProductShop(productShop);

                                    ProductShopHistory productShopHistory = new ProductShopHistory();
                                    productShopHistory.setDate(date);
                                    productShopHistory.setPriceEUR(cenaKosarice);
                                    productShopHistory.setProductShop(productShop);
                                    productShopHistory = pricesService.createProductShopHistory(productShopHistory);
                                }
                            }
                        }
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("fetchProductPricesSpecificShop successful"));
    }

}
