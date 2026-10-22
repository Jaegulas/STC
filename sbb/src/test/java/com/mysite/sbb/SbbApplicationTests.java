package com.mysite.sbb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;

import jakarta.transaction.Transactional;

/*
 @SpringBootTest : 테스트를 실행할 때 스프링 컨테이너를 실제 뛰우는 어노테이션
 				   그래서 @Autowired 로 Repository/Service 를 주입받아 실제 DB 로 테스트할 수 있습니다.
*/
@SpringBootTest
class SbbApplicationTests {
	
	@Autowired
	private QuestionRepository questionRepository;  //<--- 질문 DB작업 용도 
	
	@Autowired
    private AnswerRepository answerRepository; //<--- 답변 DB작업 용도

    @Autowired
    private QuestionService questionService; //<- 질문 기능 처리 부장 
	
	//테스트 12.  질문 300개 DB에 등록 테스트 (페이징 테스트용 데이터 만들기)
    // 테스트 메서드를 실행하면 DB 의 question 테이블에 300개의 행이 INSERT 됩니다.
	@Test
    void testJpa() {
		
        for (int i = 1; i <= 300; i++) {
            String subject = String.format("테스트 데이터입니다:[%03d]", i); //300개의 질문제목을 다르게 설정
            String content = "내용무"; 									 //300개의 질문내용을 같게 설정
            this.questionService.create(subject, content, null); //부장에게 질문제목과 질문내용을 전달해서 질문 정보 300개 INSERT 명령
        }
    }
	
	
	
	
	
	
	/*
	//테스트1. 질문 데이터들 DB에 저장 가능한지 테스트
	@Test
	void testJpa() {
		Question q1 = new Question();  			//질문 정보 하나 생성
		q1.setSubject("sbb가 무엇인가요?");		//질문 정보에 질문 제목 저장
		q1.setContent("sbb에 대해서 알고 싶습니다.");//질문 정보에 질문 내용 저장
		q1.setCreateDate(LocalDateTime.now());  //질문 정보에 질문 작성 일시 저장
		this.questionRepository.save(q1); //첫번쨰 질문 정보 하나 DB에 INSERT 실행!
		
		Question q2 = new Question();  			 //질문 정보 하나 생성
		q2.setSubject("스프링부트 모델 질문입니다.");	 //질문 정보에 질문 제목 저장
		q2.setContent("id는 자동으로 생성되나요?");  //질문 정보에 질문 내용 저장
		q2.setCreateDate(LocalDateTime.now());  //질문 정보에 질문 작성 일시 저장
		this.questionRepository.save(q2); //두번쨰 질문 정보 하나 DB에 INSERT 실행!
	}
	*/
	
	/*
	//테스트2. 질문 데이터 조회되는지 테스트하기 
	@Test
	void testJpa() {
		
		//question 테이블에 저장된 모든 질문 데이터를 조회하기 위해서 리포지터리(questionRepository)의 findAll 메서드를 사용했다.
		List<Question> all = this.questionRepository.findAll(); // select * from question
		
		//assertEquals 메서드를 사용하는데, 
		//이 메서드는 테스트에서 예상한 결과와 실제 결과가 동일한지를 확인하는 목적으로 사용한다. 
		//즉, JPA 또는 데이터베이스에서 데이터를 올바르게 가져오는지를 확인하려는 것이다. 
		//assertEquals(기댓값, 실젯값)와 같이 작성하고 기댓값과 실젯값이 동일한지를 조사한다. 
		//만약 기댓값과 실젯값이 동일하지 않다면 테스트는 실패로 처리된다
        assertEquals(2, all.size());

        //우리가 저장한 첫 번째 질문 데이터의 제목이 'sbb가 무엇인가요?' 데이터와 일치하는지도 테스트했다.
        Question q = all.get(0);
        assertEquals("sbb가 무엇인가요?", q.getSubject());
	
	}
	*/
	
	/*
	//테스트3. 질문 엔티티(Question 객체)의 기본키인 id열 값을 활용해 질문 하나 정보 조회 되는지 테스트.
	@Test
	void testJpa() {
		
	//id 열값으로 데이터를 조회하기 위해서는 리포지터리의 findById 메서드를 사용해야 한다. 
		//여기서는 questionRepository를 사용하여 데이터베이스에서 id가 1인 질문을 조회한다.
		//이때 findById의 리턴 타입은 Question이 아닌 Optional임에 주의하자. 
		//findById로 호출한 값이 존재할 수도 있고, 존재하지 않을 수도 있어서 리턴 타입으로 Optional이 사용된 것이다.
		//Optional은 그 값을 처리하기 위한(null값을 유연하게 처리하기 위한) 클래스로, isPresent() 메서드로 값이 존재하는지 확인할 수 있다.
		Optional<Question> oq = this.questionRepository.findById(1); //select * from question where id=1;
		
		//만약 isPresent()를 통해 조회된 Question 질문 엔티티 객체가  Optional 메모리에 존재한다는 것을 확인했다면,
		if(oq.isPresent()) {
			
			Question q = oq.get(); //get() 메서드를 통해 실제 Question 객체의 값을 얻는다. 
			
			//즉, 여기서는 데이터베이스에서 ID가 1인 질문을 검색하고, 
			//이에 해당하는 질문의 제목이 ‘sbb가 무엇인가요?’인 경우에 JUnit 테스트를 통과하게 된다.
			assertEquals("sbb가 무엇인가요?", q.getSubject());
		}		
	}
	*/
	
	/*
	//테스트4. 질문 제목 하나를 이용하여 질문정보 한행을 조회 할수 있는지 테스트.
	@Test
	void testJpa() {
		
		//질문 제목 하나("sbb가 무엇인가요?")를 findBySubject()메소드 호출시 전달해서
		//질문 제목에 관한 레코드(질문 행) 조회 해서 Question엔티티 객체에 담아 반환 해줍니다.
		Question q = this.questionRepository.findBySubject("sbb가 무엇인가요?");
		
		//기대값 : 조회한 질문 제목의 id 1
		//조회값 : Question q 참조변수에 저장된 Question객체의 id변수값 
		//같은지 비교
		assertEquals(1, q.getId());
	}
	*/
	
	/*
	//테스트5. 질문 제목과 내용을 이용하여 질문정보 한행을 조회 할수 있는지 테스트.
	@Test
	void testJpa() {
		
        Question q = this.questionRepository.findBySubjectAndContent("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
		
		assertEquals(1, q.getId()); //assertEquals(기대값 1, 조회한 질문의 고유id);  같으면 테스트 통과 
	}
	*/
	
	/*
	//테스트6. 질문 엔티티의 subject 열 값들 중에 특정 문자열을 포함하는 데이터가 조회 되는지 테스트.
	@Test
	void testJpa() {
		
		//질문 제목에 'sbb'로 시작하는 문자열이 포함된 조회된 질문 레코드들을(Question엔티티객체들을) List배열에 담아 반환 받자.
		List<Question> qList = this.questionRepository.findBySubjectLike("sbb%");
		
		//List배열에서  'sbb' 질문제목 문자열이 포함된  첫번째  질문 레코드(Question엔티티 객체)을 얻어 저장
        Question q = qList.get(0);
        
        //기대값으로 적은 "sbb가 무엇인가요?" 질문 제목이   조회된 질문 제목과 같으면?  테스트 통과
        assertEquals("sbb가 무엇인가요?", q.getSubject());
	}
	*/
	
	/*
	//테스트7. 질문 엔티티의 행 데이터가 제대로 수정되는지 테스트.
	@Test
	void testJpa() {
		
		//수정할 질문 엔티티(레코드)가 DB에 있는지 확인을 위해 질문 엔티티(레코드)의 id 1을 전달해 조회해 얻습니다.
		Optional<Question> oq = this.questionRepository.findById(1);
		
		//assertTrue()는 괄호 안의 값이 true(참) 인지를 테스트한다. 
		//oq.isPresent()가 false를 리턴하면 오류가 발생하고 테스트가 종료된다.
        assertTrue(oq.isPresent()); //<- 수정할 질문 레코드가 조회 되면 테스트에 통과 한다.
        
        //조회한 수정할 질문 엔티티(레코드)를 얻어 저장 
        Question q = oq.get();
        
        //조회한 수정할 질문 엔티티 객체의 subject 변수값 수정 
        q.setSubject("수정된 제목");
        
        //수정할 질문 엔티티 객체에 수정된 제목이 포함되어 있으므로 
        //save(엔티티); 를 실행하여 update(수정) 진행 합니다.
        this.questionRepository.save(q);
		
	}
	*/
	
	/*
	//테스트8. 질문 엔티티(레코드) 삭제 되는지 테스트
	//  질문 개수가 2개인지 확인 -> ID가 1번인 질문 삭제 -> 삭제 후 질문 개수가 1개인지 확인 
	@Test
	void testJpa() {
		
		//1. 현재 Question테이블에 저장된 질문 수가 2개인지 확인 합니다.
		//만약 개수가 2가 아니라면 테스트는 실패합니다.  조회된 개수가 2와 같으면 테스트는 통과합니다.
		assertEquals(2, this.questionRepository.count());
		
		//2. ID가 1번인 질문을 데이터베이스에서 찾아 조회해옵니다.
		// findById 메소드의 반환타입은 Optional<Question> 타입으로 반환합니다.
        Optional<Question> oq = this.questionRepository.findById(1);
        
		//ID가 1번인 질문 엔티티가 조회되면 테스트 통과
        assertTrue(oq.isPresent());
        
        //3. Optional에서 실제 삭제할 조회된 Question 엔티티 객체를 꺼냅니다.
        Question q = oq.get();
        
		//4. 삭제할 조회된 Question객체를 DB에서 삭제 시킵니다.
        this.questionRepository.delete(q); //delete  from question where id=1
        
        //5. 삭제 후, 질문 레코드의 총 개수가 1개인지 다시 확인 합니다.
        assertEquals(1, this.questionRepository.count());
		
	}
	*/
	
	/*
	//테스트9. 답변 등록(INSERT) 테스트 
	// 질문(id=2) 엔티티를 조회해서 찾아서, 그 질문에 답변객체를 만들어 저장(INSERT)합니다.
	@Test
	void testJpa() {
		
		//1. 먼저 ID가 2인 질문 엔티티를 데이터베이스에서 조회해  옵니다.
		//   결과는 Optional<Question> 타입입니다.  (조회가 안될수도 있기 떄문)
		Optional<Question> oq = this.questionRepository.findById(2);
			
		//2. 해당 질문 엔티티가 데이터베이스에 존재하는지 확인하기 위해 테스트 진행
		//   만약 존재 하지 않으면 테스트는 실패합니다. 존재하면 테스트는 통과.
		assertTrue(oq.isPresent());
		
		//3. Optional에서 실제 조회된 질문 엔티티 객체 Question을 꺼냅니다.
		Question q = oq.get();
		
		//4. 새로운 Answer 엔티티 객체(답변 객체)의 정보를 생성합니다.
		Answer a = new Answer();
		a.setContent("네 자동으로 생성됩니다.");  //답변 내용 설정
		
		//어떤 질문에 대한 답변인지 연결해 줍니다.
		// 답변과 질문은 N:1 관계이므로, 답변이 어떤 질문에 속하는지 반드시 알려줘야 합니다.
		a.setQuestion(q); //어떤 질문의 답변인지 알기 위해서 Question 객체가 필요하다.
						  //"q"는 위에서 조회된 id가 2인 Question 질문객체 를 뜻합니다.
		
		//답변 작성 시각을 현재 컴퓨터의 시스템 시간으로 설정
		a.setCreateDate(LocalDateTime.now());
		
		//이제 이 답변 Answer 엔티티 객체의 정보를 데이터베이스에 저장(INSERT)합니다.
		this.answerRepository.save(a);
		
	}
	*/
	
	/*
	//테스트 10. 답변 조회 시 연결된 질문 정보도 JOIN해서 조회해 오는지 테스트 
	//	  ID 1번 답변이 DB에 존재하고, 그 답변의 질문 id가 2인지 확인합니다.
	//
	//답변 엔티티 조회 할때 JOIN해서 질문 엔티티도 같이 조회  테스트
	// 이 메서드는 ID가 1번인 답변이 실제로 존재하는지 확인하고,
	// 그 답변이 ID가 2번인 질문(Question)에 연결되어 있는지를 검사합니다.
	@Test
	void testJpa() {
		
		//1. ID가 1번인 답변을 데이터베이스에서 조회 해서 가져옵니다.
		Optional<Answer> oa = this.answerRepository.findById(1);
		
		//2. 조회된 해당 답변이 존재하는지 확인 합니다.
		//   존재하면 테스트 통과 , 존재하지 않으면 테스트 실패
        assertTrue(oa.isPresent());
        
        //3. Optional에서 실제 조회된  Answer 답변 엔티티 객체의 정보를 꺼냅니다.
        Answer a = oa.get();
        
        //4.이 답변이 연결된 질문(Question)의 ID가 2인지 확인합니다.
        // 만약 연결된 질문의 ID가 2가 아니면 테스트는 실패합니다.
        assertEquals(2, a.getQuestion().getId());
		
	}
	*/
	
	// [테스트11. 설명] 지연 로딩(LAZY) 때문에 생기는 테스트 오류
		// - QuestionRepository.findById 로 질문을 가져오면 DB 세션(연결)이 끝납니다.
		// - 그 뒤에 q.getAnswerList() 를 호출하면, 답변 목록을 이제서야 DB 에서 가져오려고 하는데 세션이 이미 없습니다.
		//   → LazyInitializationException 오류가 발생합니다.
		// - 실제 웹 요청에서는 세션이 살아 있어서 생기지 않지만, 테스트 코드에서 자주 만나는 문제입니다.
		// - 해결 방법 : 테스트 메서드에 @Transactional 을 붙이면 메서드가 끝날 때까지 세션이 유지됩니다.
		/*
		테스트 에러 발생 이유
		-> 왜냐하면 QuestionRepository가 findById 메서드를 통해 Question 객체를 조회하고 나면
		   DB 세션이 끊어지기 때문이다.
		   그래서 그 이후에 실행되는 q.getAnswerList() 메서드(Question 객체로부터 answer 리스트를 구하는 메서드)는
		   세션이 종료되어 오류가 발생한다.
		   answerList는 앞서 q 객체를 조회할 때가 아니라
		   q.getAnswerList() 메서드를 호출하는 시점에 가져오기 때문에 이와 같이 오류가 발생한 것이다.
		   이렇게 데이터를 필요한 시점에 가져오는 방식을 지연(Lazy) 방식이라고 한다.
		   이와 반대로 q 객체를 조회할 때 미리 answer 리스트를 모두 가져오는 방식은 즉시(Eager) 방식이라고 한다.
		   @OneToMany, @ManyToOne 애너테이션의 옵션으로
		   fetch=FetchType.LAZY 또는 fetch=FetchType.EAGER처럼 가져오는 방식을 설정할 수 있는데,
		   이 책에서는 따로 지정하지 않고 항상 기본값(디폴트값)을 사용한다.
		   사실 이 문제는 테스트 코드에서만 발생한다. 실제 서버에서 JPA 프로그램들을 실행할 때는 DB 세션이 종료되지 않아 이와 같은 오류가 발생하지 않는다.
		   테스트 코드를 수행할 때 이런 오류를 방지할 수 있는 가장 간단한 방법은
		   다음과 같이 @Transactional 애너테이션을 사용하는 것이다.
		   @Transactional 애너테이션을 사용하면 메서드가 종료될 때까지 DB 세션이 유지된다. 코드를 수정해 보자.
	*/
	
	/*
	//테스트11. 질문에 달린 답변 목록 조회 테스트 
	// 1) ID가 1번 질문 조회    2) 질문이 DB에 존재하는지 확인  3)  질문 객체 얻어오고 
	// 4) 해당 질문에 달린 답변 목록을 가져옵니다.  5) 질문과 같이 조회된 답변이 1개인지 확인 
	// 6) 같이 조회된 답변 내용 확인
	@Test
	@Transactional
	void testJpa() {
		
		//1. ID가 2번인 질문을 데이터베이스에서 조회해서 가져옵니다.
	 	Optional<Question> oq = this.questionRepository.findById(2);
	 	
	 	//2. 해당 질문이 실제로 데이터베이스에 존재하는지 확입니다.
	 	//   존재하면 (oq.isPresent() -> true),   존재하지 않으면 (oq.isPresent() -> false)
	 	//   assertTrue(true); -> 테스트 통과
	 	//   assertTrue(false); -> 테스트 실패  질문 조회 안됨 
        assertTrue(oq.isPresent());
        
		//3. Optional에서 실제 조회된 Question 질문 객체를 꺼냅니다.
        Question q = oq.get();

        //4. 해당 질문에 달린 답변 목록을 가져옵니다.
      	// Answer는 Question과 @OneToMany 관계로 연결되어 있습니다.
        List<Answer> answerList = q.getAnswerList();

        //5. 조회된 질문에 달린 답변이 정확히 1개인지 확인합니다.
        // 예: "네 자동으로 생성됩니다." 라는 답변이 하나만 존재하는 경우
        assertEquals(1, answerList.size());
        
		//6. 그 하나뿐인 답변의 내용을 검증합니다.
		// 즉, 첫 번째(0번 인덱스)의 답변 내용이 우리가 저장했던 그 문장인지 확인합니다.
        assertEquals("네 자동으로 생성됩니다.", answerList.get(0).getContent());
	}
	*/
	
	
	
	
	
}












