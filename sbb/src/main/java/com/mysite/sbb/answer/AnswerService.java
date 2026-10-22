package com.mysite.sbb.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AnswerService { //답변 기능 처리할 서비스(부장)
	
	
	//답변 기능 DB작업할 레파지터리(사원)
	private final AnswerRepository answerRepository;
	
	
	//답변을 추천한 로그인 사람을 DB에 저장(INSERT) 하는 기능 
	public void vote(Answer answer, SiteUser siteUser) {
		
		//  Set<SiteUser>.add(로그인한 사람(답변 추천인));
        answer.getVoter().add(siteUser);
        
        this.answerRepository.save(answer); //로그인한 사람(답변 추천인)이 포함된 Answer엔티티를 전달해
        								    //DB에  INSERT 
        
    }
	
	//답변 레코드 삭제 기능
	public void delete(Answer answer) {
		
        this.answerRepository.delete(answer); //DB에 저장된 답변 레코드 DELETE 실행!
    }
	
	
	
	//답변 조회 기능
	public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

	//답변 수정 기능
    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer); //수정시 입력한 답변 내용 DB에 UPDATE
    }
	
	//답변 생성 기능		     //질문 정보 ,         답변내용,          답변내용을 작성한 작성자 정보
	public Answer create(Question question, String content, SiteUser author) {
		
		//새 답변 정보 하나 Answer 엔티티객체를 하나 생성해서 DB에 INSERT 명령
		 Answer answer = new Answer();
		 answer.setContent(content);  				//답변 생성요청시 입력한 답변 내용 저장 
		 answer.setCreateDate(LocalDateTime.now()); //답변 생성 시각 저장
		 answer.setQuestion(question);              //생성하는 답변과 연결된 질문객체 정보 저장 
		 answer.setAuthor(author);					//추가-> 답변 내용을 저장할때 작성자 정보도 함께 저장할수 있도록 저장 
		 		 
		//답변 기능 DB작업할 AnswerRepository인터페이스를 구현한 객체의 save()메소드를 호출해 
		//새 답변 정보를 DB에 생성해서 INSERT 하라 명령
		answerRepository.save(answer);
		
		// 답변이 등록된 위치로 이동하려면 반드시 DB에 등록된 답변 객체, 즉 Answer 엔티티 객체가 필요하다. 
		// create 메소드를 호출한 AnswerController.java 내부로  Answer 엔티티 객체 리턴.
		return answer;
	}
	

}








