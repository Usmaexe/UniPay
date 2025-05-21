package com.unipay.models;

import com.unipay.command.CreateBusinessCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "BUSINESS")
public class Business extends BaseEntity{

    @Column(name = "LEGAL_NAME")
    private String legalName;

    @Column(name = "TAX_ID")
    private String taxId;

    @Column(name = "REGISTRATION_NUMBER")
    private String registrationNumber;

    @Column(name = "VERIFIED")
    private boolean verified;

    @ManyToOne
    private User user;

    public static Business create(final CreateBusinessCommand command){
        final Business business = new Business();

        business.legalName = command.getLegalName();
        business.taxId = command.getTaxId();
        business.registrationNumber = command.getRegistrationNumber();

        return business;
    }
    public void update(final CreateBusinessCommand command){
        this.legalName = command.getLegalName();
        this.taxId = command.getTaxId();
        this.registrationNumber = command.getRegistrationNumber();
    }
}
