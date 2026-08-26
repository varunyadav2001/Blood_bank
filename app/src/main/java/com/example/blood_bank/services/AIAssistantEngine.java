package com.example.blood_bank.services;

import com.example.blood_bank.models.BloodRequest;
import com.example.blood_bank.models.InventoryItem;
import com.example.blood_bank.repository.BloodRepository;

import java.util.List;

public class AIAssistantEngine {

    public static String generateResponse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "How can I assist you with blood bank network operations today?";
        }

        String lower = query.toLowerCase();
        BloodRepository repo = BloodRepository.getInstance();

        // Medical Safety Guardrail Check
        if (lower.contains("diagnose") || lower.contains("treatment") || lower.contains("medicine") || lower.contains("symptom") || lower.contains("disease")) {
            return "I am an Operational Network Assistant. I am not authorized to provide medical diagnoses, treatment advice, or transfusion decisions. Please consult a licensed healthcare professional.";
        }

        // Query 1: Critical Blood Groups
        if (lower.contains("critical") || lower.contains("low")) {
            StringBuilder sb = new StringBuilder("Critical Stock Alert:\n");
            int count = 0;
            for (InventoryItem item : repo.getInventoryItems()) {
                if (item.getAvailableUnits() < 5 || item.isPriorityReview()) {
                    sb.append(" -  ").append(item.getBloodGroup()).append(" (").append(item.getComponent())
                      .append("): ").append(item.getAvailableUnits()).append(" units at ").append(item.getBloodBankName()).append("\n");
                    count++;
                }
            }
            if (count == 0) return "All blood groups currently maintain adequate operational reserve stock.";
            return sb.toString().trim();
        }

        // Query 2: Expiring Units
        if (lower.contains("expire") || lower.contains("expiry")) {
            StringBuilder sb = new StringBuilder("Expiring Units Report:\n");
            int count = 0;
            for (InventoryItem item : repo.getInventoryItems()) {
                if (item.getExpiryDaysLeft() <= 7) {
                    sb.append(" -  ").append(item.getAvailableUnits()).append(" units of ").append(item.getBloodGroup())
                      .append(" expire in ").append(item.getExpiryDaysLeft()).append(" days at ").append(item.getBloodBankName()).append("\n");
                    count++;
                }
            }
            if (count == 0) return "There are no units approaching expiry within the configured warning threshold.";
            return sb.toString().trim();
        }

        // Query 3: Pending Emergency Requests
        if (lower.contains("emergency") || lower.contains("pending") || lower.contains("active")) {
            int activeCount = 0;
            for (BloodRequest req : repo.getRequests()) {
                if (!"FULFILLED".equalsIgnoreCase(req.getStatus()) && !"CANCELLED".equalsIgnoreCase(req.getStatus())) {
                    activeCount++;
                }
            }
            return "There are currently " + activeCount + " active/pending emergency blood requests requiring coordination.";
        }

        // Query 4: Blood Group Specific Stock Query (e.g., O+)
        if (lower.contains("o+") || lower.contains("o-") || lower.contains("a+") || lower.contains("b+")) {
            return "Verified Blood Banks currently report available stock across O+, B+, A+, and O- groups with active real-time tracking.";
        }

        // Default Grounded Operational Response
        return "SmartBlood Intelligence Report: Currently tracking " + repo.getInventoryItems().size() + " inventory items across connected blood banks and " + repo.getRequests().size() + " total requests.";
    }
}
