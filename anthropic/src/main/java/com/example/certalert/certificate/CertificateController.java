package com.example.certalert.certificate;

import com.example.certalert.certificate.dto.CertificateDto;
import com.example.certalert.certificate.dto.UrlFetchRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService service;

    public CertificateController(CertificateService service) {
        this.service = service;
    }

    /**
     * List certificates visible to the caller's group.
     *
     * The spec says "descending by expiry date", so {@code desc} is the default. The UI also
     * offers {@code asc} (soonest-to-expire first) because that ordering is more useful when
     * you are looking for what to renew next.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER','MANAGER')")
    public List<CertificateDto> list(@RequestParam(defaultValue = "desc") String order) {
        CertificateService.SortOrder parsed = "asc".equalsIgnoreCase(order)
                ? CertificateService.SortOrder.ASC
                : CertificateService.SortOrder.DESC;
        return service.listForCurrentUser(parsed).stream().map(CertificateDto::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','MANAGER')")
    public CertificateDto get(@PathVariable Long id) {
        return CertificateDto.from(service.getForCurrentUser(id));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CertificateDto> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Certificate saved = service.addFromFile(file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.created(URI.create("/api/certificates/" + saved.getId()))
                .body(CertificateDto.from(saved));
    }

    @PostMapping(path = "/fetch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CertificateDto> fetch(@Valid @RequestBody UrlFetchRequest request) {
        Certificate saved = service.addFromUrl(request.url(), request.alias());
        return ResponseEntity.created(URI.create("/api/certificates/" + saved.getId()))
                .body(CertificateDto.from(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}
