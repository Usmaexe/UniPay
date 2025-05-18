package com.unipay.service.business;

import com.unipay.command.CreateBusinessCommand;
import com.unipay.models.Business;
import com.unipay.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessService {
    /** Create a new Business for the current user */
    Business create(CreateBusinessCommand command);

    /** Fetch a single Business by its ID, only if owned by current user */
    Business findById(String id);

    /** List all Businesses belonging to the current user */
    Page<Business> findForCurrentUser(Pageable pageable, User user);

    /** Update an existing Business (only mutable fields) */
    Business update(String id, CreateBusinessCommand command);

    /** Delete a Business by ID (must belong to current user) */
    void delete(String id);
}
