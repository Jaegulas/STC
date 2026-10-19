package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.mysite.sbb.DataNotFoundException;

//@Service : 아래의 QuestionService클래스의 객체를 자동생성해서 스프링컨테이너 메모리에 보관해 주는 어노테이션
//@RequiredArgsConstructor : 롬복(Lombok)이 제공하는 애너테이션으로, final이 붙은 변수을 포함하는 생성자를 자동으로 만들어 주는 역할을 한다.

@RequiredArgsConstructor
@Service  // new QuestionService();->스프링 컨테이너 객체 메모리에 보관 해두고 컨트롤러에서 의존주입받아 사용가능하게 할수 있음
public class QuestionService { //부장 
	
	//사원 
	//질문 목록과 관련된 DB작업 하는 레파지토리 인터페이스 주입(저장) 
	// -> QuestionService 객체를 자동 생성할 때 롬복으로 만들어진 생성자에 의해 questionRepository 객체가 자동으로 주입된다.
	private final QuestionRepository questionRepository;
	
	/*
	 * [getList] 지정한 페이지의 질문 목록을 조회해서 가져옵니다.
	 *
	 * 이 메서드는 질문 목록 화면이 열릴 때, 또는 하단의 페이지 번호를 누를 때 실행됩니다.
	 * 사용자가 "2" 페이지를 누르면 브라우저는 /question/list?page=1 주소를 요청하고,
	 * Controller 가 그 값(1)을 이 메서드의 page 로 넘겨 줍니다.
	 *
	 * 호출하는 곳 : QuestionController.getlist(1)
	 * 입력값      : int page
	 *               조회할 페이지 번호입니다. 0 부터 시작합니다.
	 *               0 은 첫 번째 페이지, 1 은 두 번째 페이지를 뜻합니다.
	 *               화면에서는 1페이지로 보이지만 코드에서는 0 이 첫 페이지입니다.
	 * 반환값      : Page<Question>
	 *               질문 10개 목록과, 전체 질문 수, 전체 페이지 수 같은 페이지 정보가 함께 들어 있는 객체입니다.
	 *               Controller 는 이 객체를 받아서 화면에 넘겨 줍니다.
	 */
	public Page<Question> getList(int page) {
		
		/*
		 * 조회된 질문 레코드들을 어떤 순서로 보여 줄지 정하는 정렬 기준을 담을 목록을 만듭니다.
		 * 처음에는 아무 기준도 없는 빈 목록입니다.
		 * 여러 기준을 담을 수 있도록 List 형태로 만들었습니다.
		 */
		List<Sort.Order>  sorts = new ArrayList<>();
		
		/*
		 * 빈 List에 정렬 기준을 하나 추가합니다.
		 * Sort.Order.desc("createDate") 는 작성일시(createDate) 열을 기준으로,
		 * 큰 값부터 작은 값 순서로 정렬하라는 뜻입니다.
		 * 작성일시는 날짜와 시간이므로, 큰 값이 가장 최근 시각을 의미합니다.
		 * 그래서 가장 최근에 등록된 질문이 목록 맨 앞에 오게 됩니다.
		 *
		 * desc 는 descending(내림차순)의 줄임말입니다.
		 * 반대로 asc 는 ascending(오름차순)이며, 작은 값부터 정렬합니다.
		 */
		sorts.add( Sort.Order.desc("createDate") );
		
	
		/*
		 * 페이지 요청 정보 Pageable객체를 만듭니다.
		 * PageRequest.of 의 세 가지 입력값은 다음과 같습니다.
		 *   page           : 질문 레코드를 조회해서 보고 싶은 화면의  페이지 번호입니다. 메서드가 받은 값을 그대로 씁니다.
		 *   10             : 한 페이지에 보여 줄 질문 개수입니다. 이 화면은 10개씩 보여 줍니다.
		 *   Sort.by(sorts) : 앞에서 만든 정렬 기준을 적용합니다.
		 *
		 * 여기서 중요한 점은, 이 줄에서는 아직 데이터베이스를 조회하지 않는다는 것입니다.
		 * "몇 번째 페이지를, 10개씩, 최신순으로 달라"는 요청 정보만 만들어서 pageable 에 담아 둡니다.
		 * PageRequest 는 이 요청 정보를 Pageable 이라는 규격에 맞춰 만들어 주는 클래스입니다.
		 */
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts) );
        
        //QuestionRepository(사원)의 findAll( 클릭한 페이지번호 + 한페이지 화면에 조회할 레코드수 가 저장된 Pageable객체 전달) 
        //질문 레코드 10개 조회 해 온나! 명령!  
        return this.questionRepository.findAll(pageable);
      //return new Page<Question>();
        
        
    }
	
	
	
	//새 질문 정보 DB에 추가 기능
	public void create(String subject, String content) {
		
		//DB에 추가할 새 질문 정보가 저장될 Question 엔티티 클래스의 객체 생성
		Question  q = new Question();
		q.setSubject(subject);   				//입력한 새 질문 제목 저장
		q.setContent(content);   				//입력한 새 질문 내용 저장
		q.setCreateDate(LocalDateTime.now());   //입력한 새 질문 전체 정보가 저장될 시각 저장 
		
		questionRepository.save(q);   //입력한 새 질문 정보(Question엔티티 객체의 변수정보)를 
									  //DB의 question테이블에 INSERT 합니다.		
	}
	
	
	
	//질문상세 기능
	public Question getQuestion(Integer id) { //질문 상세 내역을 조회할 질문 id를 매개변수로 전달 받음 
		
		//QuestionRepository(사원) 의 findById(id) 메소드를 호출하여 DB에 저장된 질문상세정보(레코드) 조회해서 얻기
		Optional<Question> question = questionRepository.findById(id);
		
		if(question.isPresent()) { //조회된 Question질문 엔티티 객체가 Optional보관 되어 있으면?
			
			return question.get(); //QuestionController(사장)에게 조회된 Question질문 엔티티 객체 반환(보고)
			
		}else { //조회된 Question질문 엔티티 객체가 Optional보관 되어 있지 않다면?
			
            throw new DataNotFoundException("question not found"); //질문상세정보(레코드) 조회 실패 메세지를 저장
		}
		
	}
	

	

}








