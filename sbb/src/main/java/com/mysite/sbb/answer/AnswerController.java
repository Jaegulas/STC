package com.mysite.sbb.answer;

import java.security.Principal;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequestMapping("/answer")
@RequiredArgsConstructor // final 이 붙은 변수값을 초기화 하는 생성자 자동 생성 
@Controller   //<- new AnswerController(); 자동생성되고 스프링 컨테이너에 보관
public class AnswerController { //답변 요청을 받아 처리하는 사장 

	private final QuestionService questionService; //질문 서비스 (부장)
	
	private final AnswerService answerService; //답변 서비스 (부장)
	
	private final UserService userService;  //로그인 한 답변 작성자 관련 기능 처리 서비스 (부장)
	
	
	//답변 추천인을 로그인 사용자로 DB에 INSERT 요청 기능 처리 
	//GET  http://localhost:8080/answer/vote/${answer.id}
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal, @PathVariable("id") Integer id) {
		
		//답변 추천인 DB에 추가 요청할때 전달 받은 답변 id를 넘겨서  답변 레코드 정보 조회해서 얻기
		//참고. 답변 레코드 정보에는 추천인들  Set<SiteUser> 정보도 포함되어 있다.
        Answer answer = this.answerService.getAnswer(id);
        
        //로그인한 사용자 정보 조회 해서 얻기 
        SiteUser siteUser = this.userService.getUser(principal.getName());
        
        //답변 추천인을 DB에 INSERT 명령 
        this.answerService.vote(answer, siteUser);       
        
        //답변 추천인 DB에 INSERT에 성공하면? 보여질 질문 상세페이지 리다이렉트 
        //=>리다이렉트되는 질문 상세 페이지(question_detail.html) URL에 #answer_%s를 이와 같이 삽입하여 답변 추천 버튼을 누른 위치의 앵커로 이동되게 추가한 것이다.
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(),  answer.getId());
    }
	
	
	
	
	//답변 정보를 DB에 삭제 하는 기능 처리 
	//GET http://localhost:8080/answer/delete/5
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")  
    public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
		    
		//삭제 요청한 답변 ID를 이용해 답변 레코드(엔티티)를 조회해서 가져옵니다.
		//조회되는 엔티티에는 답변 작성자 ID가 포함되어 있다.
        Answer answer = this.answerService.getAnswer(id);       
        
        //삭제 요청한 답변 ID로 조회한 답변 작성자 ID와 로그인한 사용자 ID가 다르면?
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
        	//강제로 예외 발생 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        //부장의 메소드를 호출하면서 DB에 답변 레코드 삭제 명령!
        this.answerService.delete(answer); 
        
        //답변레코드 삭제 완료한 후에는 질문 상세 페이지로 리다이렉트하도록 했다.
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }
	    
	
	 
	//수정한 답변 정보를 DB에 UPDATE하는 기능 
	//POST http://localhost:8080/answer/modify/4
	@PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
            					@PathVariable("id") Integer id, Principal principal) {
		//수정시 입력한 답변 정보 유호성 검사에 실패 하면?
        if (bindingResult.hasErrors()) {
            return "answer_form"; //다시 수정을 위해 답변내용을 입력하는 수정 요청 화면 보여주기 
        }      
        //답변 id로 답변정보를 조회 해서 가져 오는데 답변정보에는 답변작성자 아이디가 포함되어 있다.
        Answer answer = this.answerService.getAnswer(id);
        
        //조회한 답변 작성자 아이디와   로그인 해서 답변 수정을 하는 사람의 아이디가 다르면?
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); //예외 발생 
        }
        
        //조회한 답변 작성자 아이디와 로그인해서 답변을 수정하는 사람의 아이디가 같다면?
        //=> 답변 서비스(부장)에게 수정을 위해 입력한  답변 수정정보를 DB에 UPDATE해줘~ 명령
        this.answerService.modify(answer, answerForm.getContent());
        
        //답변 수정을 완료한 후에는 질문 상세 페이지로 리다이렉트하도록 했다.
        //=>리다이렉트되는 질문 상세 페이지(question_detail.html) URL에 
        //  #answer_%s를 이와 같이 삽입하여 답변 수정버튼을 누른 답변 목록 위치인? <a>앵커로 이동되게 추가한 것이다.
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }
	
	
	//답변 수정 요청하는 화면 보여주는 기능 처리 
	//GET http://localhost:8080/answer/modify/4
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal) {
		
		//수정요청 받은 답변의 id로 DB에서 답변정보 조회 해 옵니다. 
        Answer answer = this.answerService.getAnswer(id);
        
        //로그인한 사용자 아이디가 답변작성자 아이디와 다르면?
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
        	//예외 발생 시키기 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        //로그인한 사용자 아이디가 답변작성자 아이와 같다면?
        //조회된 답변 내용만 DTO에 저장 
        //이유 :  답변 내용을 수정하기 전에  DB에서 조회된 답변내용을 아래의 answewr_form.html에 보여주기 위해 
        answerForm.setContent(answer.getContent());
        
        //답변 수정 요청화면 뷰  answewr_form.html 보여주자 
        return "answer_form";
    }
	
	//답변 생성(등록) 요청을 받았을때 처리
	//POST http://localhost:8080/answer/create/2
	@PreAuthorize("isAuthenticated()") //[@PreAuthorize("isAuthenticated()")] 로그인한 사용자만 이 메서드를 실행할 수 있게 합니다.
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, 
							   @PathVariable("id") Integer id,
							   @Valid AnswerForm answerForm,    
							   BindingResult bindingResult,
							   Principal principal
								) {
								//앞서 작성한 템플릿(question_detail.html)에서 답변으로 입력한 내용(content)을 
								//얻으려고 DTO역할을 하는 AnswerForm객체를 받기 위해  AnswerForm answerForm 매개변수를 추가한 것이다. 
								//이때 유효성 검사를 위해  @Valid 어노테이션 기호를 앞에 붙였다.
								//그리고 유효성검사 성공 또는 실패 메세지를 전달해주는  BindingResult객체를 매개변수로 받기 위해
								//BindingResult bindingResult 매개변수를 작성 해 놨다.
								//
								//답변을 DB에 INSERT(저장) 할때  답변작성자 정보도 함께 저장할수 있도록 하기 위해...
								//Principal principal 매개변수 추가
								//참고. 현재 로그인한 사용자의 정보를 알려면 스프링 시큐리티가 제공하는 Principal 객체를 사용하기 위해 추가
								//     - principal.getName()을 호출하면 현재 로그인 사용자의 사용자명(ID)을 얻을수 있다.
		
		//질문 서비스(부장)에게 답변을 생성할때 쓰일 질문 객체 하나 조회해 서 얻기 위해 명령
		Question question = questionService.getQuestion(id);
		
		//스프링 시큐리티가 제공하는 principal 객체의 .getName() 통해  로그인한 사용자명(아이디)을 얻은 후,
		//로그인한 아이디를 통해 SiteUser 객체(사용자정보)를 조회 명령
		//이유 : 새 답변을 등록할 때 답변 작성자 아이디도 함께 DB에 저장하기 위해 
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		//답변 내용을 입력하지 않아 유효성 검사에 실패하면?
		if (bindingResult.hasErrors()) {
			
			//뷰페이지 question_detail.html에 조회한 질문 객체 정보를 Model에 담아 사용하기 위해  이한 줄의 코드가 있다.  
            model.addAttribute("question", question);
            
            //답변 내용을 입력하지 않아 유효성 검사에 실패했으므로 오류 메세지를 보여주기 위해 다시~ question_detail.html 뷰경로 반환 
            return "question_detail";
        }
		
		//답변 서비스(부장)에게 입력한 새 답변 정보를 DB에 추가해줘~ 명령
		//=> DB에 등록된 새 답변 정보(Answer엔티티 객체)를 create메소드로 부터 리턴 받아 저장하자.
		Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
		
		//다시 질문에 달린 새 답변을 보기 위해 질문 상세 페이지 http://localhost:8080/question/detail/2  재요청(리다이렉트) 
		//=>리다이렉트되는 질문 상세 페이지(question_detail.html) URL에 #answer_%s를 이와 같이 삽입하여 
		//  답변등록한 답변목록의 위치로 가서 브라이저에 보이게 하기위한  앵커 위치를 추가한 것이다 
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
		
							//redirect:/question/detail/답변이달린질문id#answer_등록한답변id
		
							//redirect:/question/detail/2#answer_3
		
	}
	
	
	
	
}








