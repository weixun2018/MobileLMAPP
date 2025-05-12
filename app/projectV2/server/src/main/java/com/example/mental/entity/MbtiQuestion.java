package com.example.mental.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "mbti_questions")
public class MbtiQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionText;

    @Column(name = "option_a", nullable = false)
    private String optionA;

    @Column(name = "option_b", nullable = false)
    private String optionB;

    @Column(nullable = false)
    private String dimension;
} 