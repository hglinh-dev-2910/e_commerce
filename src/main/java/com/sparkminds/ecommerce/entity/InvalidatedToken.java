package com.sparkminds.ecommerce.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invalidated_tokens")
public class InvalidatedToken {

    @Id
    private String id;

    @Column(name = "expiry_time", nullable = false)
    private Date expiryTime;
}
