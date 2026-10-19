package com.mysite.sbb.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	
	//BcryptPasswordEncoder 객체를 직접 생성하여 사용하지 않고 빈으로 등록한 Password Encoder 객체를 주입받아 사용할 수 있도록 선언
	private final PasswordEncoder passwordEncoder;
	
	//회원가입 기능 
    public SiteUser create(String username, String email, String password) {
    	
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        
        //스프링 시큐리티의 BCryptPasswordEncoder 클래스를 사용하여 암호화하여 비밀번호를 저장했다.
        //BCryptPasswordEncoder 클래스는 비크립트(BCrypt) 해시 함수를 사용하는데, 
        //비크립트는 해시 함수의 하나로 주로 비밀번호와 같은 보안 정보를 안전하게 저장하고 검증할 때 사용하는 암호화 기술이다.
        user.setPassword(passwordEncoder.encode(password));
        
        
        this.userRepository.save(user); //INSERT
        
        return user;
    }
	
	
}






