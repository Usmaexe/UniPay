package com.unipay.controller;

import com.unipay.command.CreateBusinessCommand;
import com.unipay.dto.BusinessDto;
import com.unipay.mapper.BusinessMapper;
import com.unipay.models.Business;
import com.unipay.models.User;
import com.unipay.service.authentication.AuthenticationService;
import com.unipay.service.business.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.unipay.constants.ResourcePaths.BUSINESSES;
import static com.unipay.constants.ResourcePaths.V1;

/**
 * REST controller for managing Business entities.
 */
@RestController
@RequestMapping(V1 + BUSINESSES)
@RequiredArgsConstructor
public class BusinessController {


    private final BusinessMapper businessMapper;
    private final BusinessService businessService;
    private final AuthenticationService authenticationService;

    /**
     * Create a new Business for current user.
     */
    @Operation(summary = "Create Business", description = "Creates a new business entity associated with the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Business created successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<String> createBusiness(
            @RequestBody CreateBusinessCommand command) {
        Business created = businessService.create(command);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body("Business created successfully");
    }

    /**
     * Get Business by ID.
     */
    @Operation(summary = "Get Business", description = "Retrieves a business by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found",
                    content = @Content(schema = @Schema(implementation = Business.class))),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(
            @PathVariable String id) {
        Business business = businessService.findById(id);
        return ResponseEntity.ok(business);
    }

    /**
     * Get Business of current user.
     */
    @Operation(summary = "Get My Business", description = "Retrieves the business associated with the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found",
                    content = @Content(schema = @Schema(implementation = Business.class))),
            @ApiResponse(responseCode = "404", description = "Business for user not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping()
    public ResponseEntity<Page<BusinessDto>> getMyBusiness(Pageable pageable) {
        User user = authenticationService.getCurrentUser();
        Page<Business> business = businessService.findForCurrentUser(pageable, user);
        return ResponseEntity.ok(business.map(businessMapper::toDto));
    }

    /**
     * Update existing Business.
     */
    @Operation(summary = "Update Business", description = "Updates an existing business entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business updated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBusiness(
            @PathVariable String id,
            @RequestBody CreateBusinessCommand command) {
        businessService.update(id, command);
        return ResponseEntity.ok("Business updated successfully");
    }

    /**
     * Delete Business by ID.
     */
    @Operation(summary = "Delete Business", description = "Deletes a business entity by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Business deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable String id) {
        businessService.delete(id);
        return ResponseEntity.noContent().build();
    }
}