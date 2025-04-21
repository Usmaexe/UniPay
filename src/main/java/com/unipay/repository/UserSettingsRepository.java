package com.unipay.repository;

import com.unipay.models.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
}
