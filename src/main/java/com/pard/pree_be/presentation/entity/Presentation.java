package com.pard.pree_be.presentation.entity;

import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.DialectOverride.ColumnDefault;

@Getter
@Setter
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presentation {
    @Id
    @GeneratedValue
    private UUID presentationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String presentationName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int totalPractices;
    private int totalScore;

    private boolean toggleFavorite;

    private boolean showMeOnScreen;
    private boolean showTimeOnScreen;

    @Column
    private double idealMaxTime; // 60 for 1min

    @Column
    private double idealMinTime;

    @OneToMany(mappedBy = "presentation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Practice> practices = new ArrayList<>();


    public void incrementTotalPractices() {
        this.totalPractices++;
    }

}