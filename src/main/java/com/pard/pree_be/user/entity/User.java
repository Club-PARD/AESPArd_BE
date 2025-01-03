package com.pard.pree_be.user.entity;

import com.pard.pree_be.presentation.entity.Presentation;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID userId;

    private String userName;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Presentation> presentations = new ArrayList<>();

}
