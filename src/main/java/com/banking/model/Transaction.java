package com.banking.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

	private Integer transactionId;
	private Integer accountId;
	private String transactionType;
	private BigDecimal amount;
	private String description;
	private Date transactionDate;
	private BigDecimal balanceAfter;

}