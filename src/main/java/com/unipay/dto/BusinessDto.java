package com.unipay.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents business entity information for KYB verification and onboarding.
 */
@Data
@Getter
@Setter
public class BusinessDto {
    /**
     * Legal name of the business.
     */
    private String legalName;

    /**
     * Tax identification number (TIN).
     */
    private String taxId;

    /**
     * Government-issued business registration number.
     */
    private String registrationNumber;

    /**
     * Indicates whether the business has passed KYB verification.
     */
    private boolean verified;
}
