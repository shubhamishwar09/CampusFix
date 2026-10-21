package com.campusfix;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    public ComplaintController(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @PostMapping
    public Complaint submitComplaint(@RequestBody Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @GetMapping("/{id}")
    public Complaint trackComplaint(@PathVariable Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Complaint not found"));
    }

    @GetMapping("/stats")
    public Map<String, Long> getComplaintStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", complaintRepository.count());
        stats.put("pending", complaintRepository.findAll().stream()
                .filter(c -> "Pending".equals(c.getStatus()))
                .count());
        stats.put("inProgress", complaintRepository.findAll().stream()
                .filter(c -> "In Progress".equals(c.getStatus()))
                .count());
        stats.put("resolved", complaintRepository.findAll().stream()
                .filter(c -> "Resolved".equals(c.getStatus()))
                .count());

        return stats;
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @RequestBody String status) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(status.replace("\"", ""));
        return complaintRepository.save(complaint);
    }
}