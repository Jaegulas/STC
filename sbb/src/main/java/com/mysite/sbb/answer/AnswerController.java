package com.mysite.sbb.answer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller   //<- new AnswerController(); 자동생성되고 스프링 컨테이너에 보관
public class AnswerController { //답변 요청을 받아 처리하는 사장 

	private final QuestionService questionService; //질문 서비스 (부장)
	
	private final AnswerService answerService; //답변 서비스 (부장)
	
	
	//[답변 생성] 요청을 받았을때...
	//POST http://localhost:8080/answer/create/2
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, 
							   @PathVariable("id") Integer id,
							   @Valid AnswerForm answerForm,    
							   BindingResult bindingResult
								) {
								//앞서 작성한 템플릿(question_detail.html)에서 답변으로 입력한 내용(content)을 
								//얻으려고 DTO역할을 하는 AnswerForm객체를 받기 위해  AnswerForm answerForm 매개변수를 추가한 것이다. 
								//이때 유효성 검사를 위해  @Valid 어노테이션 기호를 앞에 붙였다.
								//그리고 유효성검사 성공 또는 실패 메세지를 전달해주는  BindingResult객체를 매개변수로 받기 위해
								//BindingResult bindingResult 매개변수를 작성 해 놨다.
		
		//질문 서비스(부장)에게 답변을 생성할때 쓰일 질문 객체 하나 조회해 서 얻기 위해 명령
		Question question = questionService.getQuestion(id);
		
		//답변 내용을 입력하지 않아 유효성 검사에 실패하면?
		if (bindingResult.hasErrors()) {
			
			//뷰페이지 question_detail.html에 조회한 질문 객체 정보를 Model에 담아 사용하기 위해  이한 줄의 코드가 있다.  
            model.addAttribute("question", question);
            
            //답변 내용을 입력하지 않아 유효성 검사에 실패했으므로 오류 메세지를 보여주기 위해 다시~ question_detail.html 뷰경로 반환 
            return "question_detail";
        }
		
		//답변 서비스(부장)에게 입력한 새 답변 정보를 DB에 추가해줘~ 명령
		this.answerService.create(question, answerForm.getContent());
		
		//다시 질문에 달린 새 답변을 보기 위해 질문 상세 페이지 http://localhost:8080/question/detail/2  재요청(리다이렉트) 
		return String.format("redirect:/question/detail/%s", id);
	}
	
	
	
	
}








