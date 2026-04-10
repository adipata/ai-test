package com.example.certalert.certificate;

import com.example.certalert.certificate.parser.CertificateParseException;
import com.example.certalert.certificate.parser.CertificateParser;
import com.example.certalert.certificate.parser.ParsedCertificate;
import com.example.certalert.certificate.parser.UrlCertificateFetcher;
import com.example.certalert.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {

    private final CertificateRepository repository;
    private final CertificateParser parser;
    private final UrlCertificateFetcher fetcher;

    public CertificateService(CertificateRepository repository,
                              CertificateParser parser,
                              UrlCertificateFetcher fetcher) {
        this.repository = repository;
        this.parser = parser;
        this.fetcher = fetcher;
    }

    /* ------------------------------------------------------------------ reads */

    @Transactional(readOnly = true)
    public List<Certificate> listForCurrentUser(SortOrder order) {
        String group = SecurityUtils.currentUser().group();
        return switch (order) {
            case DESC -> repository.findAllByOwnerGroupOrderByNotAfterDesc(group);
            case ASC -> repository.findAllByOwnerGroupOrderByNotAfterAsc(group);
        };
    }

    @Transactional(readOnly = true)
    public Certificate getForCurrentUser(Long id) {
        String group = SecurityUtils.currentUser().group();
        return repository.findByIdAndOwnerGroup(id, group)
                .orElseThrow(() -> new CertificateNotFoundException(id));
    }

    /* ----------------------------------------------------------------- writes */

    @Transactional
    public Certificate addFromFile(byte[] content, String originalFilename) {
        ParsedCertificate parsed = parser.parse(content);
        String alias = (originalFilename == null || originalFilename.isBlank()) ? "uploaded.cer" : originalFilename;
        return persist(parsed, alias, CertificateSource.FILE, alias);
    }

    @Transactional
    public Certificate addFromUrl(String url, String aliasOverride) {
        ParsedCertificate parsed = fetcher.fetch(url);
        String alias = (aliasOverride == null || aliasOverride.isBlank()) ? url : aliasOverride;
        return persist(parsed, alias, CertificateSource.URL, url);
    }

    @Transactional
    public void deleteForCurrentUser(Long id) {
        Certificate existing = getForCurrentUser(id);
        repository.delete(existing);
    }

    /* ---------------------------------------------------------------- helpers */

    private Certificate persist(ParsedCertificate parsed, String alias, CertificateSource source, String sourceRef) {
        SecurityUtils.CurrentUser user = SecurityUtils.currentUser();
        Optional<Certificate> existing =
                repository.findByOwnerGroupAndFingerprintSha256(user.group(), parsed.fingerprintSha256());
        if (existing.isPresent()) {
            throw new DuplicateCertificateException(existing.get().getId(), parsed.fingerprintSha256());
        }
        Certificate entity = new Certificate(
                alias,
                parsed.subject(),
                parsed.issuer(),
                parsed.serialNumber(),
                parsed.notBefore(),
                parsed.notAfter(),
                parsed.signatureAlgorithm(),
                parsed.fingerprintSha256(),
                user.group(),
                user.username(),
                Instant.now(),
                source,
                sourceRef,
                parsed.pem()
        );
        return repository.save(entity);
    }

    public enum SortOrder { ASC, DESC }

    public static class CertificateNotFoundException extends RuntimeException {
        public CertificateNotFoundException(Long id) { super("certificate " + id + " not found"); }
    }

    public static class DuplicateCertificateException extends RuntimeException {
        private final Long existingId;
        public DuplicateCertificateException(Long existingId, String fingerprint) {
            super("certificate already exists (id=" + existingId + ", fingerprint=" + fingerprint + ")");
            this.existingId = existingId;
        }
        public Long getExistingId() { return existingId; }
    }
}
