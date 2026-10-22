package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/*
 * ============================================================
 * [람다 표현식이 무엇을 구현하는지] - 먼저 읽어 주세요
 * ============================================================
 *
 * 이 파일의 .authorizeHttpRequests(...), .csrf(...), .headers(...), .formLogin(...), .logout(...) 안에 있는
 * (이름) -> { ... } 형태가 람다 표현식입니다.
 *
 * 1) 람다가 구현하는 인터페이스
 *    Spring Security 에는 아래와 같은 인터페이스가 있습니다.
 *
 *        public interface Customizer<T> {
 *            void customize(T t);
 *        }
 *
 *    - 인터페이스: 메서드의 이름과 모양만 정해 둔 설계도입니다. 내용은 가지고 있지 않습니다.
 *    - 추상 메서드: 내용이 없는 메서드입니다. 여기서는 customize(T t) 하나뿐입니다.
 *    - 함수형 인터페이스: 추상 메서드가 딱 하나뿐인 인터페이스입니다. 람다로 구현할 수 있는 조건입니다.
 *
 * 2) 람다 = customize 메서드의 내용을 짧게 적은 것
 *    .csrf((csrf) -> csrf.ignoringRequestMatchers(...))
 *    는 Spring 이 내부적으로 다음 뜻으로 받아들입니다.
 *
 *        Customizer<CsrfConfigurer<HttpSecurity>> c = new Customizer<>() {
 *            @Override
 *            public void customize(CsrfConfigurer<HttpSecurity> csrf) {
 *                csrf.ignoringRequestMatchers(...);   // 람다 본문이 여기에 들어갑니다.
 *            }
 *        };
 *
 *    즉, 람다의 매개변수 csrf 는 customize 메서드가 받는 t 입니다.
 *    람다의 본문 { ... } 은 customize 메서드의 몸통입니다.
 *
 * 3) "오버라이딩"과 "구현"의 차이
 *    - 오버라이딩: 부모 클래스에 내용이 있는 메서드를 자식이 새 구현 코드 내용으로 덮어쓰는 것입니다.
 *    - 구현: 부모 인터페이스에 내용이 없는 메서드(추상 메서드)에 실제 내용을 채우는 것입니다.
 *    이 파일의 람다는 인터페이스의 추상 메서드를 구현합니다. 엄밀히 말하면 오버라이딩과 같은 원리이지만,
 *    부모에 내용이 없다는 점에서 구현이라고 부르는 것이 정확합니다.
 *
 * 4) 바깥쪽 메서드도 같은 원리로 연결되어 있습니다.
 *    HttpSecurity 의 .csrf 메서드는 Customizer 타입의 값을 매개변수로 받도록 정의되어 있습니다.
 *    그래서 우리가 쓴 람다를 그 자리에 넘길 수 있습니다.
 *    Spring 이 이 람다를 꺼내 CsrfConfigurer 객체를 넘기며 customize 를 호출합니다.
 *
 * 5) 다섯 개 람다의 타입 정리
 *    번호  설정 메서드              람다 매개변수 타입                                                         구현하는 메서드
 *    (1)  authorizeHttpRequests    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry   Customizer.customize(T)
 *    (2)  csrf                     CsrfConfigurer<HttpSecurity>                                                              Customizer.customize(T)
 *    (3)  headers                  HeadersConfigurer<HttpSecurity>                                                           Customizer.customize(T)
 *    (4)  formLogin                FormLoginConfigurer<HttpSecurity>                                                         Customizer.customize(T)
 *    (5)  logout                   LogoutConfigurer<HttpSecurity>                                                            Customizer.customize(T)
 *
 *    다섯 개 모두 같은 인터페이스(Customizer)의 같은 메서드(customize)를 구현합니다.
 *    매개변수 타입만 설정 종류에 따라 달라집니다.
 *    매개변수 이름(csrf, headers 등)은 아무 이름이나 써도 되며, 타입은 Spring 이 정해 줍니다.
 *
 * 6) 기준 버전
 *    이 설명은 Spring Boot 3.2.x 가 사용하는 Spring Security 6.2.x 기준입니다.
 */

/*
 * 스프링 시큐리티? 스프링 기반 웹 애플리케이션의 인증과 권한을 담당하는 스프링의 하위 프레임워크이다.
 * 
 * 인증(authenticate)? 로그인과 같은 사용자의 신원을 확인하는 프로세스를 의미.
 * 
 * 권한(authorize) ? 인증된 사용자가 우리 사이트에 일반 사용자로 접속해서 관리 할건지 
 *                                          관리자 권한으로 접속해서 사이트를 관리 할건지 권한을 의미.
 *
 * ============================================================
 * [SecurityConfig 클래스 설명]
 * ============================================================
 *
 * 이 클래스는 Spring Security 의 설정을 모아 두는 파일입니다.
 * Spring Security 는 웹 사이트의 보안 검문소 역할을 합니다.
 * 들어오는 모든 요청을 먼저 검사하고, 허락된 요청만 Controller 까지 통과시킵니다.
 *
 * 이 파일에서 정하는 내용은 다음과 같습니다.
 *   1) 어떤 주소를 누구에게 허락할지             (filterChain 메서드)
 *   2) 비밀번호를 어떤 방식으로 암호화할지        (passwordEncoder 메서드)
 *   3) 로그인 검사를 누가 담당할지               (authenticationManager 메서드)
 *
 * 현재 설정은 모든 주소를 로그인 없이 누구나 접근 가능하게 되어 있습니다.
 */


//@Configuration
//Spring 의 설정 파일이라는 표시입니다. 프로그램 시작 때 이 파일을 먼저 읽습니다.
//
//@EnableWebSecurity
//모든 웹 요청이 Spring Security 의 검사를 받도록 켭니다.
//
//@EnableMethodSecurity(prePostEnabled = true)
//Controller 메서드 단위로 권한 검사를 할 수 있게 켭니다.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)//@EnableMethodSecurity 애너테이션의 prePostEnabled = true는 
											//QuestionController와 AnswerController에서 로그인 여부를 판별할 때 사용한 
											//@PreAuthorize 애너테이션을 사용하기 위해 반드시 필요한 설정
public class SecurityConfig {
	
	// ============================================================
	// [filterChain 메서드] 요청 주소(URL)별 접근 규칙과 보안 기능을 한 번에 설정합니다.
	// ============================================================
	//
	// @Bean
	//   이 메서드가 반환하는 SecurityFilterChain객체를 Spring 이 만들어 스프링컨테이너(톰캣 메모리)에 보관하라는 표시입니다.
	//
	// HttpSecurity http
	//   Spring 이 넘겨 주는 보안 설정을 담는 도구역할을 하는 객체입니다. 여기에 보안 항목을 하나씩 붙이고, 마지막에 build() 로 완성합니다.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	
    	// 인증되지 않은 모든 요청을 허락한다는 뜻입니다. 로그인하지 않아도 모든 페이지에 접근할 수 있습니다.
    	

		// .authorizeHttpRequests(...) 등을 마침표로 이어 쓰는 방식을 메서드 체이닝이라고 합니다.
		// 각 메서드는 "이 항목도 설정하라"는 뜻이며, 설정 도구(HttpSecurity http 객체)를 다시 돌려주므로 계속 이어 쓸 수 있습니다.
        http
	        // ------------------------------------------------------------
			// [1] 클라이언트의 요청(URL) 주소별 페이지 접근 권한 설정
			// ------------------------------------------------------------
			// authorizeHttpRequests 메서드는 Customizer<...> 타입의 값을 받습니다.
			// 그 자리에 쓴 (authorizeHttpRequests) -> { ... } 가 Customizer.customize(T t) 를 구현하는 람다입니다.
			// 람다의 매개변수 authorizeHttpRequests 는 권한 규칙을 적을 수 있는 설정 객체입니다.
            .authorizeHttpRequests((authorizeHttpRequests) -> 
							        // requestMatchers(...)
									//   "어떤 요청 주소에 대해서"를 사이트에 접근 허용을 정하는 메서드입니다.
									//
									// new AntPathRequestMatcher("/**")
									//   "/**" 는 모든 주소를 뜻합니다. "/", "/question/list", "/question/detail/3" 등이 모두 포함됩니다.
									//   AntPathRequestMatcher객체는 이 요청 주소 패턴과 실제 요청 주소를 비교하는 도구입니다.
						            //
									// permitAll()
									//   "이 요청 주소들은 로그인 없이 누구나 접근해도 된다"는 뜻입니다.
									//   위 패턴과 합치면 모든 요청 주소를 누구나 접근할 수 있게 됩니다.
						            //
									//   참고: 이 설정을 빼면 Spring Security 는 기본적으로 로그인을 요구하고,
									//   로그인하지 않은 사용자에게 403(접근 금지) 오류를 보냅니다.
									//   이 설정은 개발 단계에서 편하게 쓰기 위한 것입니다.
            						authorizeHttpRequests
            								.requestMatchers(new AntPathRequestMatcher("/**"))
            								.permitAll()
            								
            ) //<---  위 [1] 클라이언트의 브라우저 요청(URL) 주소별 접근 권한 설정된 HttpSecurity보안 바구니 객체 반환  
            
			// ============================================================
			// [2] 암호표(CSRF 토큰) 검사 예외 설정
			// ============================================================
			//
			// 한 문장 요약:
			//   "/h2-console 로 시작하는 주소로 온 요청은 VIEW페이지에 설정된 암호표(CSRF 토큰)을 검사를 하지 마라." 설정!   
			//
			// ------------------------------------------------------------
			// 1. 먼저 알아둘 단어
			// ------------------------------------------------------------
			//
			//   요청(request)  : 브라우저가 서버에게 보내는 부탁입니다.
			//                    예) "글 목록 보여 줘", "이 글을 저장해 줘"
			//
			//   쿠키(cookie)   : 로그인하면 서버가 브라우저에 주는 "로그인했다"는 표식입니다.
			//                    브라우저는 같은 사이트로 요청을 보낼 때마다 이 표식을 자동으로 붙입니다.
			//
			//   암호표(CSRF 토큰) : 우리 화면(HTML 폼)에 숨겨 둔 무작위 글자입니다.
			//                    <form>태그로 요청할때 이 글자도 함께 보내야 서버가 "우리 화면에서 보낸 요청"이라고 인정합니다.
			//
			//   GET 요청       : 화면을 보여 달라는 요청입니다. (조회)
			//   POST 요청      : 데이터를 저장하거나 바꾸는 요청입니다. (글 저장, 글 삭제)
			//
			// ------------------------------------------------------------
			// 2. 정상 흐름: 우리 화면에서 글을 저장할 때
			// ------------------------------------------------------------
			//
			//   [브라우저]                                          [서버]
			//      |                                                |
			//      |  (1) 입력 화면 요청  GET /question/create       	 |
			//      |----------------------------------------------->|
			//      |                                                |  입력 화면을 만들면서
			//      |                                                |  암호표 "q8Xz" 를 숨겨 넣음
			//      |  (2) 입력 화면 응답 (숨은 값: q8Xz)            		 |
			//      |<-----------------------------------------------|
			//      |                                                |
			//      |  (3) 사용자가 글을 쓰고 [저장] 클릭            		 |
			//      |      POST /question/create                     |
			//      |      + 암호표 q8Xz 함께 전송                   	 |
			//      |----------------------------------------------->|
			//      |                                                |  받은 암호표 q8Xz 와
			//      |                                                |  자기가 만든 q8Xz 비교
			//      |                                                |  -> 같다 -> 저장 성공
			//      |  (4) 저장 완료, 목록 화면으로 이동             		 |
			//      |<-----------------------------------------------|
			//
			//
			// ------------------------------------------------------------
			// 3. 공격 흐름: 암호표가 없으면 생기는 일 (CSRF 공격)
			// ------------------------------------------------------------
			//
			//   [사용자 브라우저]                 [악성 사이트]               [우리 서버]
			//         |                                |                          |
			//         | (1) 우리 사이트에 로그인        	|                          |
			//         |  -------------------------------------------------------->|
			//         |  <-------------- 로그인 쿠키 저장 (브라우저에 보관) -------|
			//         |                                |                          |
			//         | (2) 같은 브라우저로             	|                          |
            //         |     악성 사이트를 연다          	|                          |
			//         |------------------------------->|                          |
			//         |                                |                          |
			//         | (3) 사용자 눈에 안 보이는       	|                          |
			//         |     숨은 폼이 자동 전송됨       	|                          |
			//         |     POST /question/delete/3    |                          |
			//         |<-------------------------------|                          |
			//         |                                |                          |
			//         | (4) 브라우저가 요청을 보내며    		|                          |
			//         |     로그인 쿠키를 자동으로 붙임 		|                          |
			//         |---------------------------------------------------------->|
			//         |                                |     쿠키 있음 -> 로그인한 사람
			//         |                                |     암호표 없음 -> 그래도 통과(!)
			//         |                                |     글 3번 삭제됨
			//         |                                |                          |
			//
			//   핵심: 서버가 "쿠키 값만" 믿으면, 다른 사이트에서 보낸 요청도 정상 요청이 됩니다.
			//
			//
			// ------------------------------------------------------------
			// 4. 방어 흐름: 암호표(CSRF 토큰) 를 확인하면 생기는 일
			// ------------------------------------------------------------
			//
			//   [악성 사이트의 요청]                              [우리 서버]
			//         |                                                |
			//         |  POST /question/delete/3                       |
			//         |  + 쿠키 (자동으로 붙음)                        		|
			//         |  + 암호표 ??? (악성 사이트는 모름)             		|
			//         |----------------------------------------------->|
			//         |                                                |  암호표가 없거나 틀림
			//         |                                                |  -> 요청 거절
			//         |  <-------- 403 (접근 거부) -------------------- 	|
			//
			//   악성 사이트는 우리 화면의 암호표 값을 읽을 수 없습니다.
			//   브라우저는 서로 다른 사이트의 화면 내용을 서로 읽지 못하도록 막아 두기 때문입니다.
			//
			//
			// ------------------------------------------------------------
			// 5. 이 블록(.csrf)에서 하는 일: 검사 제외 주소 목록 만들기
			// ------------------------------------------------------------
			//
			//   요청 주소가 검사 대상인지 판단하는 순서
			//
			//   요청 주소: /h2-console/login
			//         |
			//         v
			//   [ignoringRequestMatchers 목록] 에 맞는 모양이 있는가?
			//         |
			//         +-- 예 (모양: /h2-console/**) --> 암호표(CSRF 토큰) 검사 생략 -> 통과
			//         |
			//         +-- 아니오 (예: /question/create) --> 암호표(CSRF 토큰) 검사 실시
			//                                              |-- 맞음 -> 통과
			//                                              +-- 틀림 -> 거절(403)
			//
			//   주소 모양 비교 예시
			//     /h2-console          ->  포함 (검사 생략)
			//     /h2-console/login    ->  포함 (검사 생략)
			//     /h2-console/main.do  ->  포함 (검사 생략)
			//     /question/list       ->  미포함 (검사 실시)
			//
			//
			// ------------------------------------------------------------
			// 6. 코드 한 줄씩 읽는 방법
			// ------------------------------------------------------------
			//
			//   .csrf( (csrf) -> csrf.ignoringRequestMatchers( new AntPathRequestMatcher("/h2-console/**") ) )
			//     |      |        |                  |                    |
			//     |      |        |                  |                    +-- 주소 모양: /h2-console 로 시작하는 모든 요청 주소
			//     |      |        |                  +-- 주소 모양을 새로 만듦
			//     |      |        +-- "검사 제외 주소 목록에 추가하라"
			//     |      +-- 설정 도구에 붙인 이름표 (csrf)
			//     +-- "암호표(CSRF 토큰) 검사 설정을 하겠다"
			//
			//
			// ------------------------------------------------------------
			// 7. 왜 H2 콘솔만 예외로 두는가 / 주의점
			// ------------------------------------------------------------
			//
			//   H2 콘솔은 H2 라이브러리가 만든 화면이라 우리 암호표(CSRF 토큰)를 넣을 수 없습니다.
			//   그래서 개발할 때만 이 요청 주소 /h2-console/** 를 예외로 둡니다.
			//
			//   [주의] 실제 서비스를 운영할 때는 이 예외를 지워야 합니다.
			//          그대로 두면 누구나 데이터베이스 화면에 접근할 수 있는 위험이 생깁니다.
			//
			// ============================================================                      
            .csrf((csrf) -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
            
                     
			// ============================================================
			// [3] HttpResponse 응답메시지 헤더 설정 (X-Frame-Options)
			// ============================================================
			//
			// 한 문장 요약:
			//   "다른 사이트가 우리 페이지를 자기 화면 안에 끼워 넣지 못하게 하고,
			//    같은 사이트 안에서만 끼워 넣기를 허용하라"는 설정입니다.
			//
			// ------------------------------------------------------------
			// 1. 먼저 알아둘 단어
			// ------------------------------------------------------------
			//
			//   HttpResponse 응답메시지(response)  : 서버가 브라우저에게 돌려주는 결과물입니다. (화면, 데이터 등)
			//   HttpResponse 응답메시지 헤더(header)  : HttpResponse 응답메시지에 함께 붙어 가는 추가 설정 데이터입니다.
			//                        				사용자 브라우저 화면에는 보이지 않고, 브라우저가 읽고 동작을 정하는 데 쓰입니다.
			//   iframe            : 한 화면 안에 다른 화면을 끼워 넣는 html 태그입니다.
			//                        예) 어떤 사이트 화면 안에 작은 창으로 다른 사이트의 태그 디자인코드를 <iframe>태그로 삽입하는것.
			//
			//	 iframe 태그 참고 : https://coding-factory.tistory.com/892
			//
			//   클릭재킹(clickjacking) : 사용자가 모르는 사이, 숨겨진 화면의 버튼을 누르게 만드는 공격입니다.
			//
			// ------------------------------------------------------------
			// 2. HttpResponse 응답메시지 헤더(header) 데이터가 없을 때 생기는 공격 : 클릭재킹 (몰래 누르게 하기)
			// ------------------------------------------------------------
			//
			// 먼저 알아둘 것: <iframe/>태그는 "화면 안에 다른 화면을 넣는 태그"입니다.
			//   예) 어떤 사이트 페이지 안에 다른 사이트 화면이 작은 창으로 들어가 있는 것
			//
			//
			// [1단계] 사용자가 실제로 보는 화면
			//
			//   +-----------------------------+
			//   |  경품 받기!                 	|
			//   |                              |
			//   |      [ 여기를 누르세요 ]      	<- 사용자가 누르려는 버튼
			//   +-----------------------------+
			//
			//
			// [2단계] 공격자가 숨겨 둔 것
			//
			//   공격자 사이트 화면 위에, 우리 사이트를 iframe 으로 끼워 넣고 투명하게 만듭니다.
			//
			//   +-----------------------------+
			//   |  경품 받기!                    |
			//   |   +------iframe-----------+  |
			//   |   | 우리 사이트 (투명)     	 |  |
			//   |   |   [ 글 삭제 ] ← 진짜 버튼     <------- 사용자 눈에는 안 보이지만 클릭은 여기로 들어감
			//   |   +-----------------------+  |
			//   |      [ "경품 버튼" ]         <-------- 사용자가 실제로 누르는 곳은 "경품" 버튼
			//   +-----------------------------+
			//
			//   사용자는 "경품 버튼"을 눌렀다고 생각하지만,
			//   클릭이 투명한 우리 사이트의 "글 삭제" 버튼으로 요청 전달되어 글이 지워집니다.<==== 이게? 클릭재킹(clickjacking) : 사용자가 모르는 사이, 숨겨진 화면의 버튼을 누르게 만드는 공격입니다.
			//
			//
			// [3단계] X-Frame-Options 헤더가 막아 주는 것
			//
			//   우리 서버는 화면을 보낼 때 "이 화면을 다른 사이트 안에 넣지 마라"는 표시를 함께 보냅니다.
			//
			//   서버 → 브라우저:  화면 내용 + X-Frame-Options: SAMEORIGIN
			//
			//   브라우저는 이 표시를 읽고, 공격자 사이트가 우리 화면을 iframe 으로 끼워 넣으려 하면
			//   화면을 보여 주지 않고 차단합니다. 그래서 2단계의 투명 겹침이 불가능해집니다.
			//
			//
			// [SAMEORIGIN 의 뜻]
			//   같은 사이트 안에서 iframe 태그로 코드를 끼워(삽입) 넣는 것만 허용하고, 다른 사이트에서 iframe 태그로 코드를 끼워(삽입) 넣는 것은 막는 의미의 상수.
			//   H2 콘솔은 같은 사이트 안에서 화면을 끼워 넣는 구조이므로, 이 허용이 있어야 열립니다.
			//
			//
			// [한 줄 정리]
			//   헤더 없음 → 다른 사이트가 우리 화면을 겹쳐서 사용자 클릭을 빼앗을 수 있음
			//   헤더 있음 → 다른 사이트에서 우리 화면을 끼워 넣지 못함 (같은 사이트는 허용)
			//
			// ============================================================            
            .headers((headers) -> 
            		  headers.addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
            
            // ------------------------------------------------------------
			// [4] 로그인 요청 화면 설정 (formLogin)
			// ------------------------------------------------------------
			// formLogin 메서드는 매개변수 (formLogin)로 Customizer<FormLoginConfigurer<HttpSecurity>> 객체를 받습니다.
			// (formLogin) -> formLogin.loginPage(...).defaultSuccessUrl(...) 가 customize(FormLoginConfigurer t) 를 구현합니다.
			//
			// loginPage("/user/login")
			//   로그인이 필요한 곳에 로그인하지 않은 사용자가 오면 이 요청주소로 보내서 로그인 화면을 보여 줍니다.
			//   이 주소를 처리하는 Controller 와 HTML 화면을 따로 만들어야 합니다.
			//
			// defaultSuccessUrl("/")
			//   로그인 성공 후 재요청해서 이동할 재요청주소입니다. 원래 가려던 주소가 있었다면 그곳으로 먼저 보냅니다.
        	.formLogin((formLogin) -> 
			        	//(formLogin) : 로그인 설정을 적을 수 있는 도구 상자 Customizer<FormLoginConfigurer<HttpSecurity>> 객체 입니다.
				        //             Spring이 이 도구 상자Customizer<FormLoginConfigurer<HttpSecurity>> 객체를 매개변수(formLogin)여기로 넘겨 줍니다.
				        //             (formLogin) -> ... 는 "이 도구 상자를 받아서 아래 내용을 설정 하겠다"는 뜻입니다.	
        				formLogin.loginPage("/user/login")
								// loginPage() : "로그인 화면 요청 URL주소를 정하는 설정" 메소드입니다.
						        //
						        // "/user/login" : 브라우저 주소창에 이 요청 URL 치면 보이는 로그인 화면의 주소를 설정합니다.
						        //   - 로그인이 필요한 페이지에 로그인 안 한 사람이 가면
						        //     Spring Security가 이 주소로 자동 이동시킵니다.
						        //   - 이 주소의 화면(HTML)은 우리가 Controller로 직접 만들어야 합니다.
						        //   - 아이디·비밀번호를 입력하는 칸이 있는 화면이어야 합니다. 				
		                		 .defaultSuccessUrl("/")		                		 
		                		// defaultSuccessUrl() : "로그인에 성공했을 때 이동해서 보여줄 재요청 주소 설정" 메소드입니다.
							        //
							        // "/" : 사이트의 첫 화면 주소입니다.
							        //   - 로그인 성공 후 브라우저가 이 주소로 이동합니다.
							        //   - 단, 로그인 전에 원래 가려던 주소가 있었다면 그곳으로 먼저 보냅니다.
							        //     (예: /question/list 를 보려다 로그인하게 되었다면 로그인 후 /question/list 로 이동)
		              )
						// ------------------------------------------------------------
						// [5] 로그아웃 설정 (logout)
						// ------------------------------------------------------------
						// logout 메서드는 (logout) 매개변수로 Customizer<LogoutConfigurer<HttpSecurity>> 를 받습니다.
						// (logout) -> logout.logoutRequestMatcher(...).logoutSuccessUrl(...).invalidateHttpSession(...) 가
						// customize(LogoutConfigurer t) 를 구현합니다.
						//
						// logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						//   로그아웃을 실행할 주소를 "/user/logout" 으로 정합니다.
						//   주의: 이 방식은 GET 요청으로도 로그아웃되므로, 외부 링크 하나로 사용자를 로그아웃시킬 수 있습니다.
						//         가능하면 POST 요청으로만 처리하는 것이 안전합니다.
						//
						// logoutSuccessUrl("/")
						//   로그아웃이 끝나면 사이트 첫 화면을 재요청해서  이동해서 보여 줍니다.
						//
						// invalidateHttpSession(true)
						//   현재 접속 정보(세션)를 완전히 지웁니다. 세션에는 로그인한 사용자 정보가 들어 있습니다.        	
			        	.logout((logout) -> 
					        	// (logout) : 로그아웃 설정을 할수 있는 Customizer<LogoutConfigurer<HttpSecurity>> 객체를 매개변수로 넘겨 받습니다.
						        
			        			 logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
					        				// logoutRequestMatcher() : "어떤 요청 주소로 요청이 오면 로그아웃을 실행할지" 정하는 설정 하는 메소드 입니다.
									        //
									        // new AntPathRequestMatcher("/user/logout") : 주소 "/user/logout"을 뜻합니다.
									        //   - 이 주소로 요청이 오면 Spring Security가 로그아웃을 처리합니다.
									        //   - 이 주소를 처리하는 Controller는 따로 만들지 않아도 됩니다.
	
			        			 	   .logoutSuccessUrl("/")
			        			 	    // logoutSuccessUrl() : "로그아웃이 끝난 뒤 이동할 주소"를 정하는 설정입니다.
								        //
								        // "/" : 사이트의 첫 화면 주소입니다.
								        //   - 로그아웃이 끝나면 브라우저가 이 주소로 재요청해서 질문목록 조회 화면으로 이동합니다.
			        			 	   
			                           .invalidateHttpSession(true));
								        // invalidateHttpSession(true) : "세션을 지워라"라는 설정입니다.
								        //
								        // 세션 = 서버가 "이 브라우저는 누구인지" 기억해 두는 기록입니다.
								        //   - true 이면 로그아웃할 때 이 기록을 지웁니다.
								        //   - 기록이 지워지면 다음 요청부터는 로그인하지 않은 사람으로 취급됩니다.
        	
		        
		// build()
		//   지금까지 붙인 설정을 하나의 SecurityFilterChain 객체에 보관해  완성합니다.
		//   return 으로 돌려주면 Spring 이 보관하고, 이후 모든 요청 검사에 사용합니다.
		//
		// 요청이 들어올 때의 흐름
		//   1) 요청이 들어온다.
		//   2) 요청 주소를 보고 어떤 검사를 할지 정한다.
		//   3) 권한, 암호표, 헤더, 로그인 여부를 차례로 검사한다.
		//   4) 통과하면 Controller 로 전달하고, 실패하면 로그인 화면 등으로 보낸다.
        return http.build();
    }
    
	// ============================================================
	// [passwordEncoder 메서드] 비밀번호를 암호화하는 도구를 등록합니다.
	// ============================================================
	//
	// DB에 비밀번호를  그대로 저장하면 데이터베이스가 노출될 때 모든 비밀번호가 드러납니다.
	// 그래서 알아볼 수 없는 문자열(암호문)로 바꾸어 저장합니다.
	//
	// PasswordEncoder 인터페이스
	//   암호화 방법과 비교 방법을 정한 규격(인터페이스)입니다.
	//   encode(비밀번호) 메소드               : 비밀번호를 암호문으로 바꿉니다.
	//   matches(입력값, 저장된 암호문) 메소드     : 입력한 비밀번호가 저장된 암호문과 같은지 확인합니다.
	//
	// new BCryptPasswordEncoder()
	//   PasswordEncoder 를 실제로 구현한 자식클래스의 객체이며, BCrypt 방식을 씁니다.
	//     1) 한 번 암호화하면 원래 글자로 되돌릴 수 없습니다.
	//     2) 같은 비밀번호도 매번 다른 암호문이 나옵니다. 무작위 값(salt)을 섞기 때문입니다.
	//     3) 일부러 계산 시간이 걸리도록 설계되어, 여러 번 맞혀 보는 공격을 어렵게 만듭니다.
	//
	// @Bean
	//  메소드가 반환하는 PasswordEncoder인터페이스를 구현한 자식 new BCryptPasswordEncoder() 객체(빈)을 
	//  Spring 컨테이너(톰캣 메모리)에 보관해 두었다가 회원가입, 로그인 처리에서 자동으로 꺼내 쓰게 합니다.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
	// =========================================================================================
	// [authenticationManager 메서드] 로그인 검사를 하는 AuthenticationManager 객체를 빈으로 등록합니다.
	// ========================================================================================
	//
	// AuthenticationManager (인터페이스)
	//   로그인 검사(인증)를 하는 메서드 authenticate() 를 가진 인터페이스입니다.
	//   아이디와 비밀번호를 전달받아, 맞으면 인증 성공 객체를 리턴하고 틀리면 예외를 발생시킵니다.
	//
	// 이 메서드가 실행되는 시점
	//   톰캣 서버가 시작될 때 한 번 호출합니다.
	//   리턴된 AuthenticationManager 객체는 Spring 컨테이너에 빈(객체)로 등록합니다.
	//
	// 리턴된 객체 안에 자동으로 연결되는 두 객체
	//   1) UserSecurityService  : UserDetailsService 인터페이스를 구현하고 @Service 로 등록한 클래스
	//                              → 아이디로 DB 에서 회원을 찾는 loadUserByUsername() 을 가지고 있습니다.
	//   2) BCryptPasswordEncoder : 이 파일의 passwordEncoder() 메서드가 빈으로 등록한 객체
	//                              → 비밀번호를 비교하는 matches() 를 가지고 있습니다.
	//   Spring Security 가 빈 목록에서 두 객체를 찾아 연결하므로, 연결 코드를 따로 작성하지 않습니다.
	//
	// 로그인할 때 실행 순서 (POST /user/login 요청이 들어왔을 때)
	//   1) login_form.html 에서 [로그인] 버튼 → POST /user/login 요청이 서버로 갑니다.
	//   2) Spring Security 필터(UsernamePasswordAuthenticationFilter)가 요청에서 username, password 값을 꺼내
	//      AuthenticationManager 의 authenticate() 를 호출합니다.
	//   3) AuthenticationManager 안의 DaoAuthenticationProvider 가
	//      UserSecurityService.loadUserByUsername(아이디) 를 호출합니다.
	//      → DB 에서 회원을 찾아 User 객체(아이디, 저장된 암호문, 권한 목록)를 리턴받습니다.
	//      → 회원이 없으면 UsernameNotFoundException 이 발생하고 로그인 실패입니다.
	//   4) PasswordEncoder.matches(입력한 비밀번호, 저장된 암호문) 를 호출해 두 값이 맞는지 비교합니다.
	//      → BCrypt 는 저장된 암호문 안의 salt 값을 사용해 입력한 비밀번호를 같은 방식으로 계산한 뒤 비교합니다.
	//   5) 맞으면 로그인 성공 → 세션에 인증 정보 저장 → defaultSuccessUrl("/") 로 이동합니다.
	//      틀리면 로그인 실패 → /user/login?error 로 이동합니다.
	//
	// @Bean
	//   이 메서드가 리턴하는 AuthenticationManager 객체를 Spring 이 보관하라는 표시입니다.
	//
	// AuthenticationConfiguration authenticationConfiguration (클래스)
	//   Spring Security 가 인증 설정을 모아 둔 객체입니다.
	//   개발자가 new 로 만들지 않습니다. 이 메서드를 호출할 때 Spring 이 매개변수로 넣어 줍니다.  
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        
    	
    	return authenticationConfiguration.getAuthenticationManager();
    
    }

}












