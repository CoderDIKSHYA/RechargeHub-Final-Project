package com.capg.RechargeHub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String validity;
    private String description;

    private String data;
    private String type;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Operator operator;
}
