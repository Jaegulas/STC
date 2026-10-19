package com.mysite.sbb.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<SiteUser, Long> {
									 //SiteUser의 기본키 타입은 Long이므로 JpaRepository<SiteUser, Long>으로 사용했다.


	//쿼리 메소드
	//-> 가입한 사용자 ID(username)이용하여 회원레코드(SiteUser엔티티) 조회 
	Optional<SiteUser> findByusername(String username);

}
