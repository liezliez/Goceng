package id.co.bcaf.goceng.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/landing")
public class LandingPageController {

    @GetMapping("/stats")
    public Map<String, String> getStats() {
        Map<String, String> stats = new HashMap<>();
        stats.put("totalLoanDisbursed", "Rp 4 Triliun+");
        stats.put("appDownloads", "20 Juta+");
        return stats;
    }

    @GetMapping("/loan-options")
    public List<Map<String, String>> getLoanOptions() {
        return List.of(
                Map.of("amount", "2 juta", "duration", "6 bulan"),
                Map.of("amount", "12 juta", "duration", "12 bulan"),
                Map.of("amount", "30 juta", "duration", "30 bulan")
        );
    }

    @GetMapping("/testimonials")
    public List<Map<String, Object>> getTestimonials() {
        return List.of(
                Map.of("name", "Murad Dn", "age", 41, "amount", "Rp5.000.000", "message", "Pinjaman mudah, anak bisa sekolah dengan nyaman."),
                Map.of("name", "Kasriasih", "age", 48, "amount", "Rp10.000.000", "message", "Pinjaman mudah, anak bisa sekolah dengan nyaman."),
                Map.of("name", "Sarsih Handayani", "age", 37, "amount", "Rp10.000.000", "message", "Tunaiku memberikan kemudahan untuk memperoleh dana."),
                Map.of("name", "Faizal Wirya Widodo", "age", 26, "amount", "Rp5.000.000", "message", "Dana darurat terpenuhi dengan cepat.")
        );
    }
}
