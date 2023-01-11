package com.rso.microservice.api;

import com.rso.microservice.api.dto.ErrorDto;
import com.rso.microservice.api.dto.MessageDto;
import com.rso.microservice.service.AuthenticationService;
import com.rso.microservice.service.PricesService;
import com.rso.microservice.util.MDCUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prices")
@OpenAPIDefinition(info = @Info(title = "Prices API",
        description = "This is API documentation for Prices Microservice",
        version = "0.1"))
@Tag(name = "Prices")
public class PricesAPI {
    private static final Logger log = LoggerFactory.getLogger(PricesAPI.class);

    final PricesService pricesService;
    final AuthenticationService authenticationService;

    public PricesAPI(PricesService pricesService, AuthenticationService authenticationService) {
        this.pricesService = pricesService;
        this.authenticationService = authenticationService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
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
        log.info("fetchProductPrices ENTRY");
        if (!authenticationService.checkUserRoleWrapper(jwt, "Administrator")) {
            log.info("fetchProductPrices EXIT");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        pricesService.fetchPricesAllShops(fetchPictures, MDCUtil.get(MDCUtil.MDCUtilKey.MICROSERVICE_VERSION),
                MDCUtil.get(MDCUtil.MDCUtilKey.REQUEST_ID));
        log.info("fetchProductPrices EXIT");
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("fetchProductPrices in progress"));
    }

    @GetMapping(value = "/shop/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt, @PathVariable String id,
            @RequestParam(required = false, defaultValue = "false") boolean fetchPictures) {
        log.info("fetchProductPricesSpecificShop ENTRY");
        if (!authenticationService.checkUserRoleWrapper(jwt, "Administrator")) {
            log.info("fetchProductPricesSpecificShop EXIT");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        Long idShop = Long.parseLong(id);
        pricesService.fetchPricesShop(idShop, fetchPictures, MDCUtil.get(MDCUtil.MDCUtilKey.MICROSERVICE_VERSION),
                MDCUtil.get(MDCUtil.MDCUtilKey.REQUEST_ID));
        log.info("fetchProductPricesSpecificShop: EXIT");
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("fetchProductPricesSpecificShop in progress"));
    }

}
