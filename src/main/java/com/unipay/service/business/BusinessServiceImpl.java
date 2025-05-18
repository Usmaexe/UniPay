package com.unipay.service.business;


import com.unipay.command.CreateBusinessCommand;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.Business;
import com.unipay.models.User;
import com.unipay.repository.BusinessRepository;
import com.unipay.service.authentication.AuthenticationService;
import com.unipay.utils.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService{

    private final AuthenticationService authenticationService;
    private final BusinessRepository businessRepository;


    @Override
    public Business create(CreateBusinessCommand command) {
        log.info("Begin creating business with payload {}", JSONUtil.toJSON(command));
        final Business business = Business.create(command);
        log.info("Business with id {} created successfully!", business.getId());
        final User user = authenticationService.getCurrentUser();
        business.setUser(user);
        return businessRepository.save(business);
    }

    @Override
    public Business findById(String businessId) {
        log.info("Begin fetching with id {}", businessId);
        final Business business = businessRepository.findById(businessId).orElseThrow(
                () -> new BusinessException(ExceptionPayloadFactory.BUSINESS_NOT_FOUND.get())
        );
        log.info("Business with id {} fetched successfully !", businessId);
        return business;
    }

    @Override
    public Page<Business> findForCurrentUser(Pageable pageable, User user) {
        return businessRepository.getBusinessesByUser_Id(pageable, user.getId()).orElseThrow(
                () -> new BusinessException(ExceptionPayloadFactory.BUSINESS_FOR_USER_NOT_FOUND.get())
        );
    }

    @Override
    public Business update(String id, CreateBusinessCommand command) {
        final Business business = findById(id);
        business.update(command);
        business.setUpdatedAt(LocalDateTime.now());
        return businessRepository.save(business);
    }

    @Override
    public void delete(String id) {
        final Business business = findById(id);
        business.setDeleted(false);
    }
}
