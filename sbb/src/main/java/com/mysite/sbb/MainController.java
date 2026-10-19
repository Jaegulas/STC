package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller   
public class MainController {

	// 요청 URL : http://localhost:8070/sbb 요청을 하면 
	// GetMapping 어노테이션은 요청된 URL(/sbb)과의 매핑을 담당해서 매핑되는 index()메서드를 찾아 실행한다.
	@GetMapping("/sbb")
	@ResponseBody 			// @ResponseBody 애너테이션은 URL 요청에 대한 응답으로 문자열을 리턴하라는 의미로 쓰였다
	public String index() {
		return "안녕하세요 sbb에 오신 것을 환영합니다.";
	}
	
	// 요청 URL : http://localhost:8080/  요청을 하면
	// GetMapping 어노테이션은 요청된 URL(/) 과의 매핑을 담당해서 매핑되는 root()메서드를 찾아 실행한다.
	@GetMapping("/")
	public String root() {
		
		// 리턴 문자열 redirect:/question/list는 /question/list URL로 페이지를 리다이렉트(재요청)하라는 명령어이다.
		// 재요청 URL : http://localhost:8080/question/list  
		return "redirect:/question/list";
	}
	
	
}







