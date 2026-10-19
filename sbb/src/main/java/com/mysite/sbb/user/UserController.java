package com.mysite.sbb.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RequestMapping("/user")
@Controller
public class UserController {
	
	private final UserService userService;
	
	//로그인 폼 화면 요청!
	//GET http://localhost:8080/user/login
	@GetMapping("/login")
	public String login() {
		return "login_form"; //templates/login_form.html 로그인 요청 뷰화면 보여주자
	}
	
	
	
	//회원가입 폼 화면 요청!!!! 
	//GET  http://localhost:8080/user/signup  
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		
		return "signup_form";   //templates/signup_form.html 뷰화면 보여주자 
	}
	
	//회원가입 요청!!!
    @PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
    	
    	//가입을 위해 입력한 가입 정보들을 제대로 입력 하지 않아 유효성검사에 실패했을때....
	    if (bindingResult.hasErrors()) {
	            return "signup_form"; //오류 메세지를 보여줄 회원가입 요청 폼 화면으로 이동 해야 합니다. 
	    }
	
	    //회원가입 폼에서 입력한 비밀번호와 비밀번호확인 내용이 다르면?
	    if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
	    	
	    		//bindingResult.rejectValue를 사용하여 입력 받은 2개의 비밀번호가 일치하지 않는다는 오류가 발생하게 했다.
	    		//bindingResult.rejectValue의 매개변수는 순서대로 각각 bindingResult.rejectValue(변수명, 오류 코드, 오류 메시지)를 의미한다.
	            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
	            return "signup_form";
	    }
	    try {
	    	//위 두 if 를 모두 통과 했다면 입력한 가입 정보들을 서비스의 메소드를 호출하면서 전달하면서 회원가입 명령!
	        userService.create(userCreateForm.getUsername(), 
	                		   userCreateForm.getEmail(), 
	                		   userCreateForm.getPassword1());
	        
	        //사용자 ID 또는 이메일 주소가 이미 DB에 존재할 경우에는 
	        //DataIntegrityViolationException이라는 예외가 발생하므로 
	        //'이미 등록된 사용자입니다.'라는 오류 메시지가 화면에 표시하도록 했다. 
	    	}catch (DataIntegrityViolationException e) {
	    		e.printStackTrace();	    		
	    		
	    		//여기서 bindingResult.reject(오류 코드, 오류 메시지)는 UserCreateForm의 검증에 의한 오류 외에 
	    		//일반적인 오류를 발생시킬 때 사용한다.
				bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");		
				
				return "signup_form";
	    	}catch (Exception e) {
	    		e.printStackTrace();
	    		
	    		bindingResult.reject("signupFailed", e.getMessage());
	    		
	    		return "signup_form";
	    	}
	    
	    
	        //만약 가입에 성공하면 질문목록을 재요청하기 위해 redirect:"localhost:8080/" 코드 작성
	        return "redirect:/";
	    }
	
}










