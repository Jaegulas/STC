package com.mysite.sbb.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SiteUser { //회원 엔티티는 create SITE_USER 테이블 자동생성후 연결

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true) // 값을 중복되게 저장할 수 없음
	private String username;
	
	private String password;

	@Column(unique = true)  // 값을 중복되게 저장할 수 없음
	private String email;
	
	
}






