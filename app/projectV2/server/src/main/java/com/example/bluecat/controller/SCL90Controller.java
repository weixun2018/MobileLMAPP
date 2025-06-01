package com.example.bluecat.controller;

import com.example.bluecat.dto.SCL90FactorDTO;
import com.example.bluecat.dto.SCL90QuestionDTO;
import com.example.bluecat.dto.SCL90ResultDTO;
import com.example.bluecat.entity.SCL90Result;
import com.example.bluecat.service.SCL90Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scl90")
public class SCL90Controller {

    @Autowired
    private SCL90Service scl90Service;

    @GetMapping("/questions")
    public ResponseEntity<List<SCL90QuestionDTO>> getAllQuestions() {
        // 从数据库获取问题，而不是硬编码
        List<SCL90QuestionDTO> questions = scl90Service.getAllQuestions();
        return ResponseEntity.ok(questions);
    }
    
    @PostMapping("/results")
    public ResponseEntity<SCL90Result> saveResult(@RequestBody SCL90ResultDTO resultDTO) {
        SCL90Result savedResult = scl90Service.saveResult(resultDTO);
        return ResponseEntity.ok(savedResult);
    }
    
    @GetMapping("/results/{userId}")
    public ResponseEntity<SCL90Result> getResultByUserId(@PathVariable Long userId) {
        Optional<SCL90Result> resultOpt = scl90Service.getResultByUserId(userId);
        
        if (resultOpt.isPresent()) {
            return ResponseEntity.ok(resultOpt.get());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/results/{userId}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long userId) {
        scl90Service.deleteResultByUserId(userId);
        return ResponseEntity.ok().build();
    }
    
    // 新增：因子相关API
    @GetMapping("/factors")
    public ResponseEntity<List<SCL90FactorDTO>> getAllFactors() {
        List<SCL90FactorDTO> factors = scl90Service.getAllFactors();
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/{id}")
    public ResponseEntity<SCL90FactorDTO> getFactorById(@PathVariable Integer id) {
        SCL90FactorDTO factor = scl90Service.getFactorById(id);
        if (factor != null) {
            return ResponseEntity.ok(factor);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/factors/name/{factorName}")
    public ResponseEntity<SCL90FactorDTO> getFactorByName(@PathVariable String factorName) {
        SCL90FactorDTO factor = scl90Service.getFactorByName(factorName);
        if (factor != null) {
            return ResponseEntity.ok(factor);
        }
        return ResponseEntity.notFound().build();
    }
} 