package com.example.bluecat.controller;

import com.example.bluecat.dto.MbtiQuestionDTO;
import com.example.bluecat.dto.MbtiTypeDTO;
import com.example.bluecat.service.MbtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mbti")
@RequiredArgsConstructor
public class MbtiController {

    private final MbtiService mbtiService;

    @GetMapping("/questions")
    public ResponseEntity<List<MbtiQuestionDTO>> getQuestions() {
        return ResponseEntity.ok(mbtiService.getQuestions());
    }

    @GetMapping("/types/{typeCode}")
    public ResponseEntity<MbtiTypeDTO> getType(@PathVariable String typeCode) {
        return ResponseEntity.ok(mbtiService.getType(typeCode));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<Void> updateUserMbtiType(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        String mbtiType = request.get("mbtiType");
        mbtiService.updateUserMbtiType(userId, mbtiType);
        return ResponseEntity.ok().build();
    }
} 