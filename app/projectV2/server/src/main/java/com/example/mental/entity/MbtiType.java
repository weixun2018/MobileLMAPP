package com.example.mental.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "mbti_types")
public class MbtiType {
    @Id
    @Column(length = 4)
    private String typeCode;

    @Column(nullable = false)
    private String typeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String characteristics;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;
} 