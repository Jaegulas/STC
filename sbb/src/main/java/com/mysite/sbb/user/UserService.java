package com.mysite.sbb.user;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	
	//BcryptPasswordEncoder 객체를 직접 생성하여 사용하지 않고 빈으로 등록한 Password Encoder 객체를 주입받아 사용할 수 있도록 선언
	private final PasswordEncoder passwordEncoder;
	
	// ============================================================
	// [getUser 메서드] 로그인 요청한 아이디로 답볍을 작성하는 회원 정보(SiteUser 객체) 조회 기능
	// ============================================================
	// 호출하는 곳 : QuestionController.questionCreate(), AnswerController.createAnswer() 등
	//              principal.getName() 으로 얻은 로그인 사용자 아이디를 넘깁니다.
	// 매개변수    : username → 로그인한 사용자의 아이디
	// 리턴        : 찾은 SiteUser 객체 → Controller 로 돌아가서 질문·답변의 author 변수에 저장됩니다.
	public SiteUser getUser(String username) {
		
		// findByusername(username)
        //   UserRepository.java 에 선언한 조회 메서드입니다.
        //   실행되는 SQL : select ... from site_user where username = ?
        //   리턴 값      : Optional<SiteUser> → 회원이 있으면 SiteUser 가 들어 있고, 없으면 비어 있습니다.
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        
        // isPresent() : Optional 안에 조회한 회원정보(SiteUser객체)가 있으면 true 를 리턴합니다.
        if (siteUser.isPresent()) {      	
        	 // get() : Optional 안에 조회한 회원정보(SiteUser객체)를 꺼내서 호출한 UserController 로 리턴합니다.
            return siteUser.get();         
        } else {
        	// Optional 안에 조회한 회원정보(SiteUser객체)가 없으면 DataNotFoundException 예외를 발생시킵니다.
            // DataNotFoundException 클래스에 @ResponseStatus(HttpStatus.NOT_FOUND) 가 있으므로
            // 브라우저에는 404 오류 화면이 표시됩니다.
            throw new DataNotFoundException("siteuser not found");
        }
    }
	
	
	
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






