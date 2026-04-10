package com.example.certalert;

import com.example.certalert.certificate.Certificate;
import com.example.certalert.certificate.CertificateRepository;
import com.example.certalert.certificate.CertificateSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test exercising: auth, role enforcement, group isolation, file upload, listing,
 * threshold management, and alerts. Uses the real H2 datasource (seeded by DataSeeder).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CertAlertIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CertificateRepository certificateRepository;

    @BeforeEach
    void cleanCertificates() {
        // Isolate tests from one another — seeded users stay, certificates are wiped.
        certificateRepository.deleteAll();
    }

    private String tokenFor(String username) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"password\"}".formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private byte[] testCertBytes() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("test-cert.pem")) {
            assertNotNull(in, "test-cert.pem missing from test resources");
            return StreamUtils.copyToByteArray(in);
        }
    }

    @Test
    void rejectsBadCredentials() throws Exception {
        mvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedAccess() throws Exception {
        mvc.perform(get("/api/certificates")).andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointReturnsGroupAndRoles() throws Exception {
        String token = tokenFor("alice");
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.group").value("security"))
                .andExpect(jsonPath("$.roles", hasItems("VIEWER", "MANAGER")));
    }

    @Test
    void viewerCannotUpload() throws Exception {
        String bobToken = tokenFor("bob");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-cert.pem", "application/x-pem-file", testCertBytes());
        mvc.perform(multipart("/api/certificates/upload").file(file)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerUploadsAndViewerInSameGroupCanList() throws Exception {
        String alice = tokenFor("alice");
        String bob = tokenFor("bob");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test-cert.pem", "application/x-pem-file", testCertBytes());

        mvc.perform(multipart("/api/certificates/upload").file(file)
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerGroup").value("security"))
                .andExpect(jsonPath("$.uploadedBy").value("alice"))
                .andExpect(jsonPath("$.subject", containsString("test.example.com")));

        // Bob (same group, viewer) sees it.
        mvc.perform(get("/api/certificates").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].ownerGroup", everyItem(equalTo("security"))));
    }

    @Test
    void crossGroupIsolation() throws Exception {
        String alice = tokenFor("alice"); // security / MANAGER
        String carol = tokenFor("carol"); // platform / MANAGER

        MockMultipartFile file = new MockMultipartFile(
                "file", "test-cert.pem", "application/x-pem-file", testCertBytes());

        MvcResult result = mvc.perform(multipart("/api/certificates/upload").file(file)
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isCreated())
                .andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Carol is in a different group — must not see it directly.
        mvc.perform(get("/api/certificates/" + id).header("Authorization", "Bearer " + carol))
                .andExpect(status().isNotFound());

        // Carol's list must not contain Alice's cert.
        MvcResult carolList = mvc.perform(get("/api/certificates")
                        .header("Authorization", "Bearer " + carol))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json.readTree(carolList.getResponse().getContentAsString());
        for (JsonNode node : body) {
            assertNotEquals(id, node.get("id").asLong(),
                    "cross-group leak: carol should not see alice's certificate");
            assertEquals("platform", node.get("ownerGroup").asText());
        }
    }

    @Test
    void duplicateUploadReturnsConflict() throws Exception {
        String alice = tokenFor("alice");
        MockMultipartFile file = new MockMultipartFile(
                "file", "dup.pem", "application/x-pem-file", testCertBytes());

        mvc.perform(multipart("/api/certificates/upload").file(file)
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isCreated());
        mvc.perform(multipart("/api/certificates/upload").file(file)
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isConflict());
    }

    private Certificate seedCert(String alias, String group, Instant notAfter) {
        Certificate c = new Certificate(
                alias,
                "CN=" + alias,
                "CN=Test CA",
                "ABC123",
                notAfter.minus(365, ChronoUnit.DAYS),
                notAfter,
                "SHA256withRSA",
                "AA:BB:" + alias.hashCode(),
                group,
                "seed",
                Instant.now(),
                CertificateSource.FILE,
                alias,
                "-----BEGIN CERTIFICATE-----\nseed\n-----END CERTIFICATE-----"
        );
        return certificateRepository.save(c);
    }

    @Test
    void listOrderingDefaultDescending() throws Exception {
        Instant now = Instant.now();
        seedCert("near", "security", now.plus(5, ChronoUnit.DAYS));
        seedCert("far", "security", now.plus(500, ChronoUnit.DAYS));
        seedCert("mid", "security", now.plus(90, ChronoUnit.DAYS));

        String alice = tokenFor("alice");

        MvcResult desc = mvc.perform(get("/api/certificates")
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode descBody = json.readTree(desc.getResponse().getContentAsString());
        assertEquals(3, descBody.size());
        assertEquals("far", descBody.get(0).get("alias").asText());
        assertEquals("mid", descBody.get(1).get("alias").asText());
        assertEquals("near", descBody.get(2).get("alias").asText());

        MvcResult asc = mvc.perform(get("/api/certificates?order=asc")
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode ascBody = json.readTree(asc.getResponse().getContentAsString());
        assertEquals("near", ascBody.get(0).get("alias").asText());
        assertEquals("far", ascBody.get(2).get("alias").asText());
    }

    @Test
    void alertsEndpointReturnsOnlyCertsWithinThreshold() throws Exception {
        Instant now = Instant.now();
        seedCert("within", "security", now.plus(10, ChronoUnit.DAYS));
        seedCert("outside", "security", now.plus(400, ChronoUnit.DAYS));
        seedCert("otherGroup", "platform", now.plus(5, ChronoUnit.DAYS));

        String alice = tokenFor("alice");
        MvcResult res = mvc.perform(get("/api/alerts").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = json.readTree(res.getResponse().getContentAsString());
        JsonNode expiring = body.get("expiring");
        // Default threshold is 30 days — only "within" from the security group should appear.
        assertEquals(1, expiring.size());
        assertEquals("within", expiring.get(0).get("alias").asText());
    }

    @Test
    void thresholdUpdateRequiresManagerRole() throws Exception {
        String bob = tokenFor("bob");
        mvc.perform(put("/api/config/threshold")
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"thresholdDays\": 45}"))
                .andExpect(status().isForbidden());

        String alice = tokenFor("alice");
        mvc.perform(put("/api/config/threshold")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"thresholdDays\": 45}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholdDays").value(45));
    }

    @Test
    void alertsEndpointRespectsGroupScope() throws Exception {
        String alice = tokenFor("alice");
        mvc.perform(get("/api/alerts").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholdDays").isNumber())
                .andExpect(jsonPath("$.expiring").isArray());
    }
}
