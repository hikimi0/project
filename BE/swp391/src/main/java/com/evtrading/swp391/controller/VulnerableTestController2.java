package com.evtrading.swp391.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.Scanner;

@RestController
@RequestMapping("/vuln2")
public class VulnerableTestController2 {

    private static final Logger log = LoggerFactory.getLogger(VulnerableTestController2.class);

    // Hardcoded API key (S2068)
    private static final String API_KEY = "AK_live_1234567890";

    // Weak random usage (predictable)
    private final Random weakRandom = new Random(12345);

    @GetMapping("/weak-rand")
    public ResponseEntity<String> weakRand() {
        int token = weakRandom.nextInt(100000);
        log.info("Generated weak token: {}", token);
        return ResponseEntity.ok("token:" + token);
    }

    // Command injection via Runtime.exec with unsanitized input
    @GetMapping("/exec")
    public ResponseEntity<String> exec(@RequestParam String cmd) {
        try {
            // insecure: executes user-supplied command
            Process p = Runtime.getRuntime().exec(cmd);
            try (Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A")) {
                String out = s.hasNext() ? s.next() : "";
                return ResponseEntity.ok(out);
            }
        } catch (Exception e) {
            // exposing stack trace in response (sensitive)
            return ResponseEntity.status(500).body("error: " + e.toString());
        }
    }

    // Insecure deserialization example
    @PostMapping("/deser")
    public ResponseEntity<String> insecureDeser(HttpServletRequest request) {
        try (ObjectInputStream ois = new ObjectInputStream(request.getInputStream())) {
            // insecure: deserializing untrusted data
            Object obj = ois.readObject();
            return ResponseEntity.ok("Deserialized: " + String.valueOf(obj));
        } catch (Exception e) {
            // swallowed details but returning message (still risky)
            log.warn("Deserialization failed", e);
            return ResponseEntity.status(400).body("bad input");
        }
    }

    // Endpoint that returns the hardcoded API key (sensitive leak)
    @GetMapping("/leak-key")
    public ResponseEntity<String> leakKey() {
        return ResponseEntity.ok("api_key=" + API_KEY);
    }
}