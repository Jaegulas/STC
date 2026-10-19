package com.mysite.sbb.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


//@RequiredArgsConstructor
	//final 변수 userRepository 를 매개변수로 받는 생성자를 Lombok 이 자동으로 만듭니다.
	//Spring 은 서버 시작 시 이 생성자를 호출하면서 UserRepository 객체를 넣어 줍니다.
//
//@Service
	//서버 시작 시 Spring 이 이 클래스의 객체를 만들어 보관(빈 등록)합니다.
	//SecurityConfig.java 의 authenticationManager() 가 만든 AuthenticationManager 는
	//UserDetailsService 를 구현한 빈을 찾아 연결하므로, 이 클래스가 로그인 검사에 사용됩니다.
@RequiredArgsConstructor
@Service

//implements UserDetailsService
//UserDetailsService 인터페이스를 구현하겠다는 선언입니다.
//이 인터페이스에는 내용이 없는 추상 메서드 loadUserByUsername() 이 있으므로
//이 클래스에서 반드시 메서드 내용을 작성해야 합니다. 작성하지 않으면 컴파일 오류가 납니다.
public class UserSecurityService implements UserDetailsService{

	//DB 의 SITE_USER 테이블을 조회할 때 사용합니다.(사원) 객체 저장 
	private final UserRepository userRepository;
	
	
	// @Override : UserDetailsService 인터페이스의 추상 메서드를 구현한다는 표시입니다.
    //
    // 호출하는 곳 : AuthenticationManager 내부의 DaoAuthenticationProvider (Spring Security)
    // 호출 시점   : 로그인 화면(login_form.html)에서 [로그인] 버튼을 눌러 POST /user/login 요청이 왔을 때
    // 매개변수    : String username → 로그인 화면에서 입력한 아이디
    // 리턴 타입   : UserDetails → 인터페이스 타입입니다. 실제로는 이를 구현한 User 객체를 리턴합니다.
    // 리턴하는 곳 : DaoAuthenticationProvider 로 돌아갑니다.
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//[1단계] DB 에서 로그인 요청시 입력한 아이디로 조회
		//   실행되는 곳 : UserRepository.java 의 findByusername() (실행 코드는 Spring Data JPA 가 자동 생성)
        //   실행되는 SQL: select ... from site_user where username = ?
		//   리턴 값     : Optional<SiteUser> → 회원이 있으면 SiteUser 객체가 들어 있고, 없으면 비어 있습니다.
		Optional<SiteUser> _siteUser = this.userRepository.findByusername(username);
		
		//[2단계] 로그인 요청시 입력한 아이디로 조회한 회원이 없으면? 예외 발생
		// isEmpty() : Optional 이 비어 있으면 true 를 리턴합니다. (Java 11 이상에서 사용 가능)
        if (_siteUser.isEmpty()) {       	
        	// throw : 예외를 발생시켜 loadUserByUsername메서드를 즉시 끝냅니다. 아래 코드는 실행되지 않습니다.
            // 예외는 호출한 곳(DaoAuthenticationProvider)으로 전달되고,
            // Spring Security 는 로그인 실패로 처리하여 /user/login?error 로 리다이렉트합니다.
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
		//[3단계] Optional 에서 SiteUser 객체 꺼내기
        // get() : Optional 안에 들어 있는 SiteUser 객체를 리턴합니다.
        // siteUser 변수에는 조회된 회원의 id, username, password(암호문), email 값이 들어 있습니다.
        SiteUser siteUser = _siteUser.get();
        
        //[4단계] 권한 목록 만들기 
        // List<GrantedAuthority> : GrantedAuthority 타입의 객체만 저장하는 목록입니다.
        // new ArrayList<>()      : 비어 있는 목록 객체를 만듭니다.
        List<GrantedAuthority> authorities = new ArrayList<>();
		
        // "admin".equals(username)
        //   로그인 요청시 입력한 아이디가 문자열 "admin" 과 같은지 비교합니다. 같으면 true 를 리턴합니다.
        if ("admin".equals(username)) {
        	
        	// UserRole.ADMIN.getValue()
            //   UserRole.java 의 ADMIN 상수에 저장된 문자열 "ROLE_ADMIN" 을 리턴합니다.
            // new SimpleGrantedAuthority("ROLE_ADMIN")
            //   "ROLE_ADMIN" 문자열로 권한 객체를 만듭니다.
            // authorities.add(...)
            //   만든 권한 객체를 목록에 추가합니다.
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
            
        } else { //관리가 admin 계정이 아닌 일반 사용자 계정으로 로그인 요청 했다면?
        	
        	// 아이디가 "admin" 이 아니면 실행됩니다.
            // UserRole.USER.getValue() 는 문자열 "ROLE_USER" 를 리턴합니다.
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
		
        // [5단계] Spring Security 용 User 객체를 만들어 리턴
        // User 는 Spring Security 의 클래스입니다. 우리가 만든 SiteUser 클래스와 다릅니다.
        //
        // User 생성자에 전달하는 값 3개
        //   siteUser.getUsername() : 아이디
        //   siteUser.getPassword() : DB 에 저장된 비밀번호 암호문 (회원가입 때 BCrypt 로 암호화한 값)
        //   authorities            : 4단계에서 만든 권한 목록
        //
        // 리턴하는 곳 : DaoAuthenticationProvider
        // 리턴 후 처리: DaoAuthenticationProvider 가 PasswordEncoder.matches(입력한 비밀번호, 암호문)를 호출해 비교합니다.
        //   - 일치하면 로그인 성공 → 세션에 인증 정보 저장 → defaultSuccessUrl("/") 로 이동
        //   - 다르면 로그인 실패 → /user/login?error 로 이동
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
	}
	
	
}







