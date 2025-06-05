package com.carshare.rentalsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "rental_reviews")
public class RentalReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OverallImpression overallImpression;

    @Column(length = 1000)
    private String comment;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rentalReview",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalReviewMedia> mediaFiles = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum OverallImpression {
        EXCELLENT,
        GOOD,
        NEUTRAL,
        BAD,
        TERRIBLE
    }
}
