package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@Controller 애너테이션 기호는 HelloController 클래스가 컨트롤러의 기능을 수행한다는 의미 입니다.
@Controller
public class HelloController {
	 
	@GetMapping("/hello")  // http://localhost:8070/hello URL 요청이 발생하면 hello메서드가 샐행됨을 의미한다.
	@ResponseBody          // hello 메소드의 return "Hello World"의 결과를 브라우저로 바로 보내 응답 시키는 역할을한다. 
	public String hello() {
		return "Hello Spring Boot";   //<- 클라이언트가 요청한 브라우저로 바로 보내 출력 
	}
	
	
  
}
