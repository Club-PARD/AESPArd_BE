package com.pard.pree_be.practice.entity;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.presentation.entity.Presentation;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(name = "Practice.graph", attributeNodes = @NamedAttributeNode("analysis"))
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Use AUTO or IDENTITY
    private UUID id;

    private String practiceName;

    @ManyToOne
    @JoinColumn(name = "presentation_id", nullable = false)
    private Presentation presentation;

    private LocalDateTime createdAt;
    private int totalScore;
    private String videoKey;

    @Lob
    private byte[] audioFile;

    @Column(nullable = false)
    private String audioFilePath;

    private int eyePercentage;

    @OneToOne
    private Analysis analysis;
}
