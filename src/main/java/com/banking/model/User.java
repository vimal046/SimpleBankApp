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
@NoArgsConstructor
@AllArgsConstructor
public class User {

	private Integer userId;
	private String username;
	private String password;
	private String email;
	private String role;
	private Integer customerId;
	private boolean active;
	private Date createdDate;

}