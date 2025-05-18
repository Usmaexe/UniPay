package com.unipay.repository;

import com.unipay.models.Business;
import com.unipay.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;


@Repository
public interface BusinessRepository extends JpaRepository<Business, String> {
    Optional<Page<Business>> getBusinessesByUser_Id(Pageable pageable, String userId);
}
