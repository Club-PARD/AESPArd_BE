package com.pard.pree_be.practice.entity;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.presentation.entity.Presentation;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Use AUTO or IDENTITY
    private UUID id;

    @Column(nullable = false)
    private String practiceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id", nullable = false)
    private Presentation presentation;

    @Column(nullable = false)
    private LocalDateTime practiceCreatedAt;


    @Column
    private int totalScore;

    @Column
    private String videoKey;

    @Lob
    @Column(name = "audio_file", columnDefinition = "LONGBLOB")
    private byte[] audioFile;


    @Column
    private String audioFilePath;

    @Column
    private int eyePercentage;

    @OneToMany(mappedBy = "practice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Analysis> analyses = new ArrayList<>();


}