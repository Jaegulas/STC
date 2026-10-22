package com.mysite.sbb.question;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

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
	private final QuestionService questionService;  //질문 정보 관련
	
	// userService 객체도 @RequiredArgsConstructor에 의해 생성자 방식으로 주입된다.
	private final UserService userService; 			//가입된 사용자 정보 관련
	
	//===========================================================
	//질문 추천 요청 기능 처리 ( GET /question/vote/${question.id} )
	//호출 위치 : question_detail.html 의 자바스크립트 코드
	//===========================================================
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
		
		//[추천] 버튼을 클릭하여 질문 추천 요청한 질문id를 넘겨서 질문레코드 조회 해서 가져옴 
		//      질문 레코드에는 추천인들 Set<SiteUser> 정보도 포함되어 있다.
        Question question = this.questionService.getQuestion(id);
        
        //추천 기능도 로그인한 사람만 사용할수 있도록 로그인한 사람의 아이디를 넘겨서 회원 레코드 정보 조회 해서 가져옴 
        SiteUser siteUser = this.userService.getUser(principal.getName());
        
        //QuestionService의 vote 메서드를 호출하여 로그인 사용자(siteUser)를 추천인(voter)으로 저장했다.
        this.questionService.vote(question, siteUser);
        
        //로그인 사용자 (SiteUser)가 추천인으로 DB에 INSERT 되었다면 ??
        //질문 상세 화면을 재요청 합니다. 
        return String.format("redirect:/question/detail/%s", id);
    }
	
	
	
	//============================================================
	//질문 삭제 요청 기능 처리  (GET  /question/delete/5 )
	//호출 위치 : question_detail.html 의 자바스크립트 코드 
	//============================================================
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
		
		//매개변수 Integer id 로 받은 삭제할 질문 id로 질문 엔티티 객체(Question)정보 조회해 오기 
        Question question = this.questionService.getQuestion(id);
        
        //로그인한 사용자와 질문 엔티티 객체에 저장된 질문 작성자 아이디가 다르면?
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
        	
        	//예외 상태 코드 와 예외 메세지 를 저장해서 예외 강제로 발생시키기 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        
        //questionService 부장의 메소드를 호출하면서 질문 레코드 삭제 명령
        this.questionService.delete(question);
        
        //질문 레코드 삭제에 성공하면 조회한 질문목록 페이지 리다이렉트(재요청) 
        return "redirect:/";
    }
	
	
	
	//=========================================================
	//질문 정보 수정 요청 기능 처리  (POST  /question/modify/{id} )
	//호출 위치  :  question_form.html
	//=========================================================
	@PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, 
            						Principal principal, @PathVariable("id") Integer id) {
		
		//질문 정보 수정시 유효성 검사에 실패하면?
        if (bindingResult.hasErrors()) {
        	//질문 정보 수정 요청하는 화면으로 이동 
            return "question_form"; 
        }
        //질문 정보 수정시 유효성 검사에 성공하면?
        //수정할 질문 정보 id로 질문 정보 하나 조회 명령해서 가져옵니다.
        Question question = this.questionService.getQuestion(id);
        
        //로그인한 사용자와 수정하려는 질문의 작성자가 동일하지 않으면? 예외 코드와 예외 메세지 저장 
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        
        //로그인한 사용자와 수정하려는 질문정보를 작성한 작성자가 동일하면?
        //questionService 부장의  modify메소드 호출해서 DB에 수정 요청 명령합니다.
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        
        
        //질문 정보 수정에 성공하면 리다이렉트(재요청)을 통해 질문 상세 화면 보여주기 
        return String.format("redirect:/question/detail/%s", id);
    }
	
	
	
	
	// ==========================================================================
    // 질문 수정 화면 보기 (GET /question/modify/{id})
    // 호출 위치 : question_detail.html 의 [수정] 링크
    // 응답 화면 : question_form.html (제목·내용이 채워진 상태)
    // ==========================================================================
	 @PreAuthorize("isAuthenticated()") // 메서드를 실행하기 전에 로그인 여부를 검사 합니다.
	 									// 로그인 하지 않았으면 이 메소드를 실행하지 않고 로그인 요청화면(/user/login)로 재요청을 보냅니다.
	 @GetMapping("/modify/{id}")
	 public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
																							 //Principal principal : 현재 로그인한 사용자 정보입니다.
																					         //스프링 시큐리티가 넣어 주며, principal.getName() 으로 로그인 아이디를 꺼낼 수 있습니다.
	    
		//질문 번호(id)로 DB 에서 질문 한 건을 조회합니다.
        //호출 흐름 : questionModify → questionService.getQuestion(id) → QuestionRepository → DB
        //조회된 Question 엔티티 객체를 변수 question 에 저장합니다.
		 Question question = this.questionService.getQuestion(id);
	     
		//로그인한 사용자와 질문 작성자의 아이디가 같은지 비교합니다.
	        //     question.getAuthor().getUsername() : 질문 작성자의 아이디
	        //     principal.getName()                : 로그인한 사용자의 아이디
	        //     앞의 ! 는 "같지 않다"라는 뜻이므로, 두 아이디가 다르면 아래 if 블록이 실행됩니다.
	     if(!question.getAuthor().getUsername().equals(principal.getName())) {
	    	 
		    	//작성자가 아니면 예외를 던집니다.
		            //     HttpStatus.BAD_REQUEST : HTTP 400(잘못된 요청) 응답입니다.
		            //     예외가 던져지면 이 아래 코드는 실행되지 않고 메서드가 즉시 끝납니다.
		            //     수정 화면은 열리지 않고 오류 응답이 브라우저로 나갑니다.
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");          
	     }
	     
	     //작성자가 맞을 때만 여기까지 내려옵니다.
	        //DB에서 조회한 질문 정보(기존 제목)을 questionForm 의 subject 에 넣습니다.
	        //나중에 템플릿의 제목 입력칸에 기존 제목이 채워집니다.
	     questionForm.setSubject(question.getSubject());
	     
	     //DB에서 조회한 질문 정보(기존 내용)을 questionForm 의 content 에 넣습니다.
	        //나중에 템플릿의 내용 입력칸에 기존 내용이 채워집니다.
	     questionForm.setContent(question.getContent());
	     	     
	     //"question_form" 은 templates/question_form.html 파일을 뜻합니다.
         //questionForm(DTO) 각 변수에 값이 채워진 상태로 템플릿에 전달되어 수정 화면이 브라우저에 보입니다.
	     return "question_form";
	}	
	
	
	
	// ============================================================
	// [@PreAuthorize("isAuthenticated()")] 로그인한 사용자만 이 메서드를 실행할 수 있게 합니다.
	// ============================================================
	//
	// [왜 필요한가]
	//   아래 메서드는 principal.getName() 으로 로그인한 사용자 아이디를 꺼내 씁니다.
	//   Principal 객체는 로그인한 경우에만 Spring Security 가 매개변수로 넣어 줍니다.
	//   로그인하지 않은 상태에서 이 메서드가 실행되면 principal 이 null 이므로
	//   principal.getName() 에서 NullPointerException 오류가 발생합니다.
	//   → 이 오류를 막기 위해 로그인하지 않은 요청은 메서드 실행 전에 차단합니다.
	//
	// [동작 방식]
	//   isAuthenticated() : 로그인했으면 true, 안 했으면 false
	//   - true  → 메서드가 정상 실행됩니다.
	//   - false → 메서드가 실행되지 않고 로그인 화면으로 이동합니다.
	//
	// [로그인하지 않은 상태에서 요청했을 때 실행 순서]
	//   1) 브라우저가 /question/create 를 요청합니다.
	//   2) Spring Security 가 메서드 실행 직전에 isAuthenticated() 를 검사합니다. → false
	//   3) 메서드를 실행하지 않고, SecurityConfig.java 의 loginPage("/user/login") 로 이동시킵니다.
	//   4) 로그인에 성공하면 처음 요청했던 /question/create 로 다시 이동합니다.
	//
	// [사용 조건]
	//   SecurityConfig.java 클래스에 @EnableMethodSecurity(prePostEnabled = true) 가 있어야 동작합니다.
	//   이 설정이 없으면 @PreAuthorize 가 무시되어 로그인 검사를 하지 않습니다.
	//
	// [적용한 메서드]
	//   - POST /question/create : 질문 등록 저장 (principal 사용)
	@PreAuthorize("isAuthenticated()")
	//==================================새 질문 등록 요청==================================================================== 
	//POST http://localhost:8080/question/create
	@PostMapping("/create")		 //1.question_form.html(질문 등록 요청화면)에서 입력한 질문 제목, 내용 매개변수로 얻을때
								 //2.DTO 역할을 하는 QuestionForm클래스의 객체를 자동생성해서 각 인스턴스변수에 자동 저장후
								 //  매개변수 QuestionForm  questionForm으로 전달 받게 되는데.....
								 //3. @Valid 에 의해 QuestionForm객체의 변수에  입력한 질문 제목, 내용이 제대로 입력되었는지 
								 //   유효성 검사를 한후 매개변수 QuestionForm  questionForm으로 전달 받게 된다. 
								 //4.  BindingResult 매개변수는 @Valid 애너테이션으로 검증이 수행된 결과를 의미하는 객체이다.
								 //    BindingResult 매개변수는 항상 @Valid 매개변수 바로 뒤에 위치해야 한다. 
								 //    만약 두 매개변수의 위치가 정확하지 않다면 @Valid만 적용되어 입력값 검증 실패 시 400 오류가 발생한다.
								 //5.  Principal principal 매개변수로 넘어 오는 Principal 객체는
								 //    스프링 시큐리티에서 제공하는 객체로 로그인한 사용자명(아이디)를 얻기 위해 매개변수를 추가한 것이다.
	public String questionCreate(@Valid  QuestionForm  questionForm, BindingResult bindingResult, Principal principal) {
		
		//5.bindResult.hasErrors()를 호출하여 오류가(유효성 검사 실패)있는 경우에는 
		//  다시 제목과 내용을 작성하는 화면으로 돌아가도록 했고
		if(bindingResult.hasErrors()) {
			return "question_form";    //  /templates/question_form.html  뷰 경로
		}
		
		//스프링 시큐리티가 제공하는 principal 객체의 .getName() 통해 로그인한 사용자명(아이디)을 얻은 후 
		//로그인한 아이디를 통해 SiteUser 객체(회원정보)를 조회하기 위해 명령!
		//이유 : 새 질문 레코드를 DB에 등록(INSERT)할때 사용자명(아이디)도 함께 INSERT 하기 위해 
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		
		//6.오류(유효성 검사에 통과 했을 경우에만) 질문이 등록되도록 만들었다.		
		//  QuestionService(부장)의 create 메소드 호출시 입력한 질문제목,내용, 질문작성자 아이디를 매개변수로 전달해서 
		//   새 질문 정보를 DB에 INSERT 해줘! 명령
		questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
		
		//7. 새 질문 DB의 question 테이블에 INSERT(저장)후 질문목록 조회 재요청을 하기 위해 작성. 
        return "redirect:/question/list"; 
    }
	 
	
	
	// ============================================================
	// [@PreAuthorize("isAuthenticated()")] 로그인한 사용자만 이 메서드를 실행할 수 있게 합니다.
	// ============================================================
	//
	// [왜 필요한가]
	//   아래 메서드는 principal.getName() 으로 로그인한 사용자 아이디를 꺼내 씁니다.
	//   Principal 객체는 로그인한 경우에만 Spring Security 가 매개변수로 넣어 줍니다.
	//   로그인하지 않은 상태에서 이 메서드가 실행되면 principal 이 null 이므로
	//   principal.getName() 에서 NullPointerException 오류가 발생합니다.
	//   → 이 오류를 막기 위해 로그인하지 않은 요청은 메서드 실행 전에 차단합니다.
	//
	// [동작 방식]
	//   isAuthenticated() : 로그인했으면 true, 안 했으면 false
	//   - true  → 메서드가 정상 실행됩니다.
	//   - false → 메서드가 실행되지 않고 로그인 화면으로 이동합니다.
	//
	// [로그인하지 않은 상태에서 요청했을 때 실행 순서]
	//   1) 브라우저가 /question/create 를 요청합니다.
	//   2) Spring Security 가 메서드 실행 직전에 isAuthenticated() 를 검사합니다. → false
	//   3) 메서드를 실행하지 않고, SecurityConfig.java 의 loginPage("/user/login") 로 이동시킵니다.
	//   4) 로그인에 성공하면 처음 요청했던 /question/create 로 다시 이동합니다.
	//
	// [사용 조건]
	//   SecurityConfig.java 클래스에 @EnableMethodSecurity(prePostEnabled = true) 가 있어야 동작합니다.
	//   이 설정이 없으면 @PreAuthorize 가 무시되어 로그인 검사를 하지 않습니다.	
	@PreAuthorize("isAuthenticated()")
	//====================================질문 등록하는 화면 요청===================================
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
	//GET http://localhost:8080/question/list?page=0
	//GET http://localhost:8080/question/list?page=0&kw=검색어
	
    @GetMapping("/list")  
    public String list(Model model, 
    				   @RequestParam(value = "page", defaultValue = "0") int page,
    				   @RequestParam(value = "kw",   defaultValue = "") String kw ) {
    	
    	//Model 객체 : 브라우저로 응답할 데이터를 보관하는 메모리   
    	
    	//1. QuestionService(부장) 객체의 getList() 메소드를 호출하여 DB에 저장된 질문목록 조회 명령
    	Page<Question> paging = this.questionService.getList(page, kw); 
    	
    
    	//2. 조회한 질문목록(List)이 저장된 Page객체 정보를  question_list.html (VIEW)에 전달해서 브라우저에 보여주기 위해 
    	//   Model 객체 메모리에 담아 둡니다.
    	model.addAttribute("paging", paging);
    	//2.1. 질문 목록 화면에서 입력한 검색어를 화면에 그대로 유지하기 위해 model.addAttribute("kw", kw)로 kw값을 저장했다.
    	model.addAttribute("kw", kw);
    	
    	//3. 조회한 질문목록(List)를 보여줄 뷰경로를 작성하면 아래와 같이 해석해서 실행합니다.
        return "question_list";  //  /templates/question_list.html  뷰 경로를 반환해서
        						 //  question_list.html 파일에 작성한 코드를 브라우저로 보여줌
    }


}









