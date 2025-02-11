package com.rh4.services;

import com.rh4.entities.Undertaking;
import com.rh4.repositories.UndertakingRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UndertakingService {
    @Autowired
    private UndertakingRepo undertakingRepo;

    public Undertaking getLatestUndertakingForm() {
        return undertakingRepo.findTopByOrderByCreatedAtDesc().orElse(null);
    }

    public boolean hasInternAccepted(String internId) {
        return undertakingRepo.findByInternId(internId)
                .map(Undertaking::isAccepted)
                .orElse(false);
    }

    @Transactional
    public void acceptUndertaking(String internId) {
        Undertaking form = undertakingRepo.findByInternId(internId)
                .orElseThrow(() -> new RuntimeException("Undertaking Form not found for this intern"));

        form.setAccepted(true);
        form.setAcceptedAt(LocalDateTime.now());

        undertakingRepo.save(form);
    }

    // Fetch all undertaking forms
    public List<Undertaking> getAllForms() {
        return undertakingRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Optional<Undertaking> getFormById(Long id) {
        return undertakingRepo.findById(id);
    }

    public void updateUndertaking(Long id, String content) {
        Undertaking undertaking = undertakingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Undertaking form not found"));

        undertaking.setContent(content);
        undertaking.setCreatedAt(LocalDateTime.now());
        undertakingRepo.save(undertaking);
    }
}
