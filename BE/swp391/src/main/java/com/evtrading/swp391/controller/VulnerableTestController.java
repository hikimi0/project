package com.evtrading.swp391.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vuln")
public class VulnerableTestController {

    private static final Logger log = LoggerFactory.getLogger(VulnerableTestController.class);

    // Hardcoded credential (S2068)
    private static final String PASSWORD = "admin123";

    @PersistenceContext
    private EntityManager em;

    // SQL injection via string concat (S2077)
    @GetMapping("/sql")
    public ResponseEntity<String> sql(@RequestParam String username) {
        String sql = "SELECT id, username FROM users WHERE username = '" + username + "'";
        List<?> rows = em.createNativeQuery(sql).getResultList();
        return ResponseEntity.ok(rows.toString());
    }

    // Logging sensitive data (security hotspot)
    @PostMapping("/log")
    public ResponseEntity<String> logAuth(@RequestHeader HttpHeaders headers, @RequestBody(required = false) String body) {
        String token = headers.getFirst("Authorization");
        log.info("Received Authorization header: {}", token); // sensitive
        return ResponseEntity.ok("ok " + body);
    }

    // Swallowing exception (S108)
    @GetMapping("/swallow")
    public String swallow() {
        try {
            int x = 1 / 0;
        } catch (Exception e) {
            // intentionally empty
        }
        return "done";
    }







    
}