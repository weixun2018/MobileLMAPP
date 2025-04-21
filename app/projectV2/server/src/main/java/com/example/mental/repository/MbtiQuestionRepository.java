package com.example.mental.repository;

import com.example.mental.entity.MbtiQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MbtiQuestionRepository extends JpaRepository<MbtiQuestion, Long> {
} 