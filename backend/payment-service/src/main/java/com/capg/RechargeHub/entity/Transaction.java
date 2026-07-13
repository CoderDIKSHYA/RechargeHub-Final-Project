/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : Transaction
 * DESCRIPTION:
 *   JPA entity representing a payment transaction.
 *   Maps to the "transactions" table in the MySQL database.
 * ================================================================ */
package com.capg.RechargeHub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rechargeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String status;

    private LocalDateTime transactionTime;

    public Transaction() {}

    public Transaction(Long id, Long rechargeId, Long userId, BigDecimal amount, String paymentMethod, String status, LocalDateTime transactionTime) {
        this.id = id;
        this.rechargeId = rechargeId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionTime = transactionTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
}
