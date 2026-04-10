package com.example.certalert.certificate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /** Spec-literal: "list certificates, descending by expiry date". */
    List<Certificate> findAllByOwnerGroupOrderByNotAfterDesc(String ownerGroup);

    /** Alert-oriented ordering — soonest-to-expire first. Used by the alert scan and the UI filter. */
    List<Certificate> findAllByOwnerGroupOrderByNotAfterAsc(String ownerGroup);

    /** Lookup with group enforcement baked in — prevents cross-group reads. */
    Optional<Certificate> findByIdAndOwnerGroup(Long id, String ownerGroup);

    /** Used by the scheduled scan across all groups. */
    List<Certificate> findAllByNotAfterBeforeOrderByNotAfterAsc(Instant cutoff);

    /** Used by the alert endpoint for a specific group. */
    List<Certificate> findAllByOwnerGroupAndNotAfterBeforeOrderByNotAfterAsc(String ownerGroup, Instant cutoff);

    /** Deduplication on re-upload within the same group. */
    Optional<Certificate> findByOwnerGroupAndFingerprintSha256(String ownerGroup, String fingerprintSha256);
}
