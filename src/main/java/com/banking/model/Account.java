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
public class Account {

	private Integer accountId;
	private Integer customerId;
	private String accountNumber;
	private BigDecimal balance;
	private String accountType;
	private Date createdDate;

	private Customer customer;

}