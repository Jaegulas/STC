package com.mysite.sbb.question;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysite.sbb.answer.AnswerForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * ==========================================================================
 * [QuestionController 클래스 = /question 으로 시작하는 요청을 받아 처리하는 클래스]
 * ==========================================================================
 *
 * ── 주소 → 메서드 → 역할 (한눈에 보기) ───────────────────────────────────────
 *   GET  /question/list            → list()            질문 목록 (검색, 페이징)
 *   GET  /question/create          → questionCreate()  질문 등록 화면 보기
 *   POST /question/create          → questionCreate()  질문 등록 저장
 *   GET  /question/detail/{id}     → detail()          질문 상세 + 답변 목록
 *   GET  /question/modify/{id}     → questionModify()  질문 수정 화면 보기
 *   POST /question/modify/{id}     → questionModify()  질문 수정 저장
 *   GET  /question/delete/{id}     → questionDelete()  질문 삭제
 *   GET  /question/vote/{id}       → questionVote()    질문 추천
 *
 * ── 클래스에 붙은 애너테이션 ─────────────────────────────────────────────
 *
 *   @Controller
 *     - 웹 요청을 받는 클래스라고 스프링에 알립니다.
 *     - 메서드가 돌려준 문자열은 "화면 이름"(예: "question_list") 또는 "redirect:주소" 로 해석됩니다.
 *
 *   @RequestMapping("/question")
 *     - 이 클래스의 모든 주소 앞에 "/question" 을 붙입니다.
 *     - 예) @GetMapping("/list") → 실제 주소는 /question/list
 *
 *   @RequiredArgsConstructor
 *     - final 변수 두 개(questionService, userService)를 받는 생성자를 Lombok 이 만듭니다.
 *     - 스프링이 이 생성자를 부를 때 두 Service 를 넣어 줍니다. (생성자 주입)
 */

//@RequiredArgsConstructor는 
//-롬복(Lombok)이 제공하는 애너테이션으로, 
// final이 붙은 변수을 포함하는 생성자를 자동으로 만들어 주는 역할을 한다.


@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController { //사장 
	
	//부장
	// questionService 객체도 @RequiredArgsConstructor에 의해 생성자 방식으로 주입된다.
	private final QuestionService questionService;
	
	//새 질문 등록 요청 
	//POST http://localhost:8080/question/create
	@PostMapping("/create")		 //1.question_form.html(질문 등록 요청화면)에서 입력한 질문 제목, 내용 매개변수로 얻을때
								 //2.DTO 역할을 하는 QuestionForm클래스의 객체를 자동생성해서 각 인스턴스변수에 자동 저장후
								 //  매개변수 QuestionForm  questionForm으로 전달 받게 되는데.....
								 //3. @Valid 에 의해 QuestionForm객체의 변수에  입력한 질문 제목, 내용이 제대로 입력되었는지 
								 //   유효성 검사를 한후 매개변수 QuestionForm  questionForm으로 전달 받게 된다. 
								 //4.  BindingResult 매개변수는 @Valid 애너테이션으로 검증이 수행된 결과를 의미하는 객체이다.
								 //    BindingResult 매개변수는 항상 @Valid 매개변수 바로 뒤에 위치해야 한다. 
								 //    만약 두 매개변수의 위치가 정확하지 않다면 @Valid만 적용되어 입력값 검증 실패 시 400 오류가 발생한다.
	public String questionCreate(@Valid  QuestionForm  questionForm, BindingResult bindingResult) {
		
		//5.bindResult.hasErrors()를 호출하여 오류가(유효성 검사 실패)있는 경우에는 
		//  다시 제목과 내용을 작성하는 화면으로 돌아가도록 했고
		if(bindingResult.hasErrors()) {
			return "question_form";    //  /templates/question_form.html  뷰 경로
		}
		
		//6.오류(유효성 검사에 통과 했을 경우에만) 질문이 등록되도록 만들었다.		
		//  QuestionService(부장)의 create 메소드 호출시 입력한 질문제목,내용을 매개변수로 전달해서 
		//   새 질문 정보를 DB에 INSERT 해줘! 명령
		questionService.create(questionForm.getSubject(), questionForm.getContent() );
		
		//7. 새 질문 DB의 question 테이블에 INSERT(저장)후 질문목록 조회 재요청을 하기 위해 작성. 
        return "redirect:/question/list"; 
    }
	 
	
	//질문 등록하는 화면 요청
	//GET http://localhost:8080/question/create
	@GetMapping("/create")
	public String questionCreate(QuestionForm questionForm) {
		return "question_form";//  /templates/question_form.html  뷰 경로를 반환해서
								   //question_form 파일(질문 등록하는 화면)에 작성한 코드를 브라우저로 보여줌
	}
	
	
	
	//질문 상세 조회 요청
	//GET http://localhost:8080/question/detail/2
	@GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm) {
		
		//1. QuestionService(부장) 객체의 getQuestion(id); 메소드를 호출하여  질문 id로 상세 조회 요청 명령!
		Question question = this.questionService.getQuestion(id);
		
    	//2. 조회한 질문 상세 내역 하나를  question_detail.html (VIEW)에 전달해서 브라우저에 보여주기 위해 
    	//   Model 객체 메모리에 담아 둡니다.
		model.addAttribute("question", question);
		
		//3. 조회한 질문 상세 내역 하나(Question 엔티티객체)를 보여줄 뷰경로를 작성하면 아래와 같이 해석해서 실행합니다.
        return "question_detail";  //  /templates/question_detail.html  뷰 경로를 반환해서
		 						   //  question_detail.html 파일에 작성한 코드를 브라우저로 보여줌
    }
	
	/*
	 * ------------------------------------------------------------
	 * [list] 질문 목록 화면을 보여줍니다.
	 * ------------------------------------------------------------
	 *
	 * 요청 주소:
	 *   GET /question/list                 → 1페이지(page 값 없음 → 기본값 0) 표시
	 *   GET /question/list?page=1          → 2페이지 표시
	 *
	 * 실행 순서:
	 *   1) 브라우저가 주소를 요청합니다.
	 *   2) 주소 뒤의 ?page=값 을 읽어서 page 변수에 넣습니다.
	 *   3) QuestionService.getList(page) 를 호출해 해당 페이지의 질문 목록을 받습니다.
	 *   4) 받은 목록을 model 에 담습니다.
	 *   5) "question_list" 라는 이름의 HTML 화면을 열어 결과를 보여 줍니다.
	 */
	//GET http://localhost:8080/question/list
	//GET http://localhost:8080/question/list?page=1
    @GetMapping("/list")  
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
    	
    	//Model 객체 : 브라우저로 응답할 데이터를 보관하는 메모리 
    	
    	//1. QuestionService(부장) 객체의 getList() 메소드를 호출하여 DB에 저장된 질문목록 조회 명령
    	Page<Question> paging = this.questionService.getList(page);
    	
    
    	//2. 조회한 질문목록(List)이 저장된 Page객체 정보를  question_list.html (VIEW)에 전달해서 브라우저에 보여주기 위해 
    	//   Model 객체 메모리에 담아 둡니다.
    	model.addAttribute("paging", paging);
    	
    	//3. 조회한 질문목록(List)를 보여줄 뷰경로를 작성하면 아래와 같이 해석해서 실행합니다.
        return "question_list";  //  /templates/question_list.html  뷰 경로를 반환해서
        						 //  question_list.html 파일에 작성한 코드를 브라우저로 보여줌
    }


}









