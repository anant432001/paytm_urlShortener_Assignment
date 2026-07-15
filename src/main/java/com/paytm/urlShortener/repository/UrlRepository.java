package com.paytm.urlShortener.repository;

import com.paytm.urlShortener.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortCode(String shortCode);

    Optional<UrlEntity> findByOriginalUrl(String originalUrl);

    boolean existsByShortCode(String shortCode);

}