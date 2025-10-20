package com.banking.model;

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
public class Customer {

	private Integer customerId;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private Date createdDate;


}