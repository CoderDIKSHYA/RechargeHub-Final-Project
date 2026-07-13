/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : User
 * DESCRIPTION:
 *   JPA entity representing a registered user in the system.
 *   Maps to the "users2" table in the MySQL database.
 *   Stores credentials, role, and profile information.
 *   createdAt is auto-set on first persist via @PrePersist.
 * ================================================================ */
package com.capg.RechargeHub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    private String phoneNumber;

    private String profilePictureUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
    private boolean isVerified = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
