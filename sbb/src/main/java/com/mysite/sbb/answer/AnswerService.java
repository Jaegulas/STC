package com.mysite.sbb.answer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mysite.sbb.question.Question;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AnswerService { //답변 기능 처리할 서비스(부장)
	
	
	//답변 기능 DB작업할 레파지터리(사원)
	private final AnswerRepository answerRepository;
	
	//답변 생성 기능
	public void create(Question question, String content) {
		
		//새 답변 정보 하나 Answer 엔티티객체를 하나 생성해서 DB에 INSERT 명령
		 Answer answer = new Answer();
		 answer.setContent(content);  				//답변 생성요청시 입력한 답변 내용 저장 
		 answer.setCreateDate(LocalDateTime.now()); //답변 생성 시각 저장
		 answer.setQuestion(question);              //생성하는 답변과 연결된 질문객체 정보 저장 
		 
		//답변 기능 DB작업할 AnswerRepository인터페이스를 구현한 객체의 save()메소드를 호출해 
		//새 답변 정보를 DB에 생성해서 INSERT 하라 명령
		answerRepository.save(answer);
		
		
	}
	

}








