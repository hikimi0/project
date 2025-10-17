package com.evtrading.swp391.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

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

    // XSS: trả HTML có chèn trực tiếp tham số người dùng
    @GetMapping(value = "/xss", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> xss(@RequestParam String q) {
        String html = "<h1>Search</h1><div>q=" + q + "</div>";
        return ResponseEntity.ok(html);
    }

    // Path Traversal: đọc file từ đường dẫn người dùng nhập
    @GetMapping("/path")
    public ResponseEntity<String> readFile(@RequestParam String file) throws Exception {
        String content = Files.readString(Paths.get(file));
        return ResponseEntity.ok(content);
    }

    // Open Redirect: chuyển hướng tới URL tùy ý
    @GetMapping("/redirect")
    public ResponseEntity<Void> redirect(@RequestParam String target) {
        return ResponseEntity.status(302).header("Location", target).build();
    }

    // SSRF: gọi tới URL người dùng cung cấp
    @GetMapping("/ssrf")
    public ResponseEntity<String> ssrf(@RequestParam String url) {
        try (Scanner s = new Scanner(new URL(url).openStream()).useDelimiter("\\A")) {
            String body = s.hasNext() ? s.next() : "";
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("fail: " + e.getMessage());
        }
    }

    // Weak crypto: dùng MD5
    @PostMapping("/md5")
    public ResponseEntity<String> md5(@RequestBody String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok("md5=" + bytesToHex(digest));
    }

    // Insecure cookie: set cookie không HttpOnly/Secure
    @GetMapping("/cookie")
    public ResponseEntity<String> cookie(@RequestParam String sid) {
        return ResponseEntity.ok()
                .header("Set-Cookie", "SESSION=" + sid + "; Path=/")
                .body("set");
    }

    // ReDoS risk: biên dịch regex từ input
    @PostMapping("/regex")
    public ResponseEntity<String> regex(@RequestParam String pattern, @RequestBody String payload) {
        boolean matched = Pattern.compile(pattern).matcher(payload).find();
        return ResponseEntity.ok("matched=" + matched);
    }

    // Env leak: lộ biến môi trường
    @GetMapping("/env")
    public ResponseEntity<String> env(@RequestParam String name) {
        return ResponseEntity.ok(String.valueOf(System.getenv(name)));
    }

    // CORS *: cho phép mọi origin
    @CrossOrigin(origins = "*")
    @GetMapping("/public-info")
    public String publicInfo(@RequestParam(defaultValue = "world") String name) {
        return "Hi " + name;
    }

    // helper cho MD5
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

}