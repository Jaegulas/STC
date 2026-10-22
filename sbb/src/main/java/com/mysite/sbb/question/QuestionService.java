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
import com.mysite.sbb.user.SiteUser;



import com.mysite.sbb.answer.Answer;

//------------------------------------------------------------
//필요한 클래스 가져오기 (import)
//아래 코드에서 사용하는 JPA 관련 클래스의 위치를 알려 줍니다.
//------------------------------------------------------------

//조인 대상과 기준 테이블을 나타내는 클래스들입니다.
//Root: 기준이 되는 엔티티(Question)를 가리키는 객체입니다.
//Join: 두 엔티티를 이어 붙인 결과를 가리키는 객체입니다.
//JoinType: 조인 종류(LEFT, INNER 등)를 고르는 값입니다.
//CriteriaQuery: 지금 만들고 있는 조회문 전체입니다.
//CriteriaBuilder: 조건(like, or 등)을 만드는 도구 모음입니다.
//Predicate: 참/거짓 조건 한 덩어리입니다. SQL의 WHERE 조건에 해당합니다.
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;


//@Service : 아래의 QuestionService클래스의 객체를 자동생성해서 스프링컨테이너 메모리에 보관해 주는 어노테이션
//@RequiredArgsConstructor : 롬복(Lombok)이 제공하는 애너테이션으로, final이 붙은 변수을 포함하는 생성자를 자동으로 만들어 주는 역할을 한다.

@RequiredArgsConstructor
@Service  // new QuestionService();->스프링 컨테이너 객체 메모리에 보관 해두고 컨트롤러에서 의존주입받아 사용가능하게 할수 있음
public class QuestionService { //부장 
	
	//사원 
	//질문 목록과 관련된 DB작업 하는 레파지토리 인터페이스 주입(저장) 
	// -> QuestionService 객체를 자동 생성할 때 롬복으로 만들어진 생성자에 의해 questionRepository 객체가 자동으로 주입된다.
	private final QuestionRepository questionRepository;
	
	//search 메서드는 검색어를 가리키는 kw를 입력받아 쿼리의 조인문과 where문을 Specification 객체로 생성하여 리턴하는 메서드이다.
	private Specification<Question> search(String kw) {
		
		
		 // return: 아래에서 만든 Specification 객체를 호출한 곳으로 돌려줍니다.
	     // new Specification<>() { ... }
	     //   - 이름 없는 클래스를 만들면서 바로 객체를 생성합니다. 이를 "익명 클래스"라고 부릅니다.
	     //   - 중괄호 { } 안에 Specification이 요구하는 메서드(toPredicate)를 직접 구현합니다.
	     //   - <>는 다이아몬드 연산자입니다. 앞의 <Question>을 자동으로 채워 넣습니다.
        return new Specification<>() {
        	
        	 // serialVersionUID: 객체를 파일로 저장하거나 네트워크로 보낼 때 버전을 확인하는 번호입니다.
            private static final long serialVersionUID = 1L;
            
            // @Override: "부모 인터페이스(Specification)의 메서드를 여기서 구현한다"는 표시입니다.
            @Override	
				             // toPredicate: 실제 WHERE 조건을 만드는 메서드입니다. 조건 객체(Predicate)를 돌려줍니다.
					         // 매개변수 3개는 다음과 같습니다.
					         //   Root<Question> q        : 기준 테이블(질문)입니다. q.get("subject")처럼 컬럼을 꺼냅니다.
					         //   CriteriaQuery<?> query  : 조회문 전체입니다. 중복 제거 같은 설정을 여기에 합니다.
					         //   CriteriaBuilder cb      : 조건을 만드는 도구입니다. cb.like, cb.or 등을 호출합니다.
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
            	
        	   // query.distinct(true)
               //   - 조회결과에서 같은 질문이 여러 줄로 나오지 않게 중복을 제거합니다. SQL의 DISTINCT입니다.
               //   - 이유: 답변이 3개인 질문은 아래 조인 때문에 3줄로 늘어나기 때문입니다.
                query.distinct(true);  // 중복을 제거 
                
                // [u1] 질문 작성자 정보와 연결한 객체
	             // Join<Question, SiteUser>
	             //   - 앞의 타입(Question)은 출발 엔티티, 뒤의 타입(SiteUser)은 연결할 엔티티입니다.
	             // q.join("author", JoinType.LEFT)
	             //   - Question 엔티티의 author 속성을 따라 SiteUser와 조인합니다.
	             //   - "author"는 Question 클래스 안에 적힌 필드 이름과 같아야 합니다.
	             //   - JoinType.LEFT(아우터 조인): 작성자 정보가 없는 질문도 결과에서 빠지지 않게 합니다.
	             //   - u1 객체는 "질문 작성자"의 아이디(username)를 검색할 때 사용합니다.
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                
	             // [a] 답변 정보와 연결한 객체
	             // Join<Question, Answer>
	             //   - 출발은 Question, 연결 대상은 Answer입니다.
	             // q.join("answerList", JoinType.LEFT)
	             //   - Question 엔티티의 answerList 속성(이 질문에 달린 답변 목록)을 따라 Answer와 조인합니다.
	             //   - LEFT(아우터 조인): 답변이 하나도 없는 질문도 결과에 남깁니다.
	             //   - a 객체는 "답변 내용(content)"을 검색할 때 사용합니다.
	             //   - 주의: 답변이 여러 개면 질문 한 개가 답변 수만큼 여러 줄로 늘어납니다. 위의 distinct가 이를 정리합니다.
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                
                // [u2] 답변 작성자 정보와 연결한 객체
	             // Join<Answer, SiteUser>
	             //   - 출발은 Answer, 연결 대상은 SiteUser입니다. 앞의 u1과 출발 엔티티가 다릅니다.
	             // a.join("author", JoinType.LEFT)
	             //   - 앞에서 만든 a(답변)를 출발점으로 삼아 author 속성을 따라 SiteUser와 조인합니다.
	             //   - 질문이 아니라 답변의 작성자이므로 q가 아니라 a에서 조인합니다.
	             //   - u2 객체는 "답변 작성자"의 아이디(username)를 검색할 때 사용합니다.
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                
                // cb.or(조건1, 조건2, 조건3, 조건4, 조건5)
	             //   - OR 검색입니다. 다섯 조건 중 하나라도 참이면 그 질문이 검색 결과에 포함됩니다.
	             //   - SQL의 WHERE 조건1 OR 조건2 OR ... 에 해당합니다.
                return  cb.or( 
		                		 // 조건 1: 질문 제목 검색
			                     // q.get("subject") : 질문의 subject(제목) 값을 꺼냅니다.
			                     // "%" + kw + "%"   : 검색어 앞뒤에 % 기호를 붙입니다.
			                     //                    % 는 "글자가 몇 개든 상관없음"을 뜻하는 와일드카드입니다.
			                     //                    예) kw가 "자바"이면 "%자바%" 는 앞, 뒤, 가운데 어디에 "자바"가 있어도 맞습니다.
			                     // cb.like(컬럼, 패턴) : 컬럼 값이 패턴과 맞는지 검사합니다. SQL의 LIKE입니다.
                				cb.like(q.get("subject"), "%" + kw + "%"),      // 제목 
                				
                				// 조건 2: 질문 내용 검색
                				// q.get("content") : 질문의 content(내용) 값을 꺼냅니다.
		                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용 
		                        
		                        // 조건 3: 질문 작성자 아이디 검색
			                    // u1.get("username") : 질문 작성자의 username(아이디) 값을 꺼냅니다.
		                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자 
		                        
		                        // 조건 4: 답변 내용 검색
			                    // a.get("content") : 답변의 content(내용) 값을 꺼냅니다.
		                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용 
		                        
		                        // 조건 5: 답변 작성자 아이디 검색
			                    // u2.get("username") : 답변 작성자의 username(아이디) 값을 꺼냅니다.
		                        cb.like(u2.get("username"), "%" + kw + "%")     // 답변 작성자 		                        
                		);   
            }
        };
    }
	
	
	
	//추천인을 DB에 저장할수 있는 기능 
    public void vote(Question question, SiteUser siteUser) {
    	
    	//로그인한 사용자를 질문 엔티티(Question 엔티티)에 추천인으로 DB에 INSERT(저장)  
        question.getVoter().add(siteUser);      
        this.questionRepository.save(question); //로그인한 추천인이 보관된 Question 엔티티객체의 변수정보들을 INSERT
    }
	
	
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
    										//질문목륵을 조회 하기 위해 입력한 검색어 
	public Page<Question> getList(int page, String kw) { 
		
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
        
        
        //질문 목록을 조회 하기 위해 입력한 검색어(kw 매개변수로 전달받은 값)을 search메소드 호출시 전달하여
        //검색 조건 SQL이 저장된 Specification 부모인터페이스를 구현한 자식 익명객체를 반환받아 spec변수에 저장 
     //방법1.
     // Specification<Question>  spec   = search(kw);
        
              
        //QuestionRepository(사원)의 findAll( 클릭한 페이지번호 + 한페이지 화면에 조회할 레코드수 가 저장된 Pageable객체 전달) 
        //질문 레코드 10개 조회 해 온나! 명령!  
      //방법1.
      //return this.questionRepository.findAll(spec, pageable);       
        
      //방법2. @Query 어노테이션으로 작성한 메소드 호출
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }
	

	//새 질문 정보 DB에 추가 기능   새 질문제목,      새 질문 내용,     새 질문 작성자 
	public void create(String subject, String content, SiteUser user) {
		
		//DB에 추가할 새 질문 정보가 저장될 Question 엔티티 클래스의 객체 생성
		Question  q = new Question();
		q.setSubject(subject);   				//입력한 새 질문 제목 저장
		q.setContent(content);   				//입력한 새 질문 내용 저장
		q.setCreateDate(LocalDateTime.now());   //입력한 새 질문 전체 정보가 저장될 시각 저장 
		q.setAuthor(user);						//추가-> 입력한 새 질문 작성자 정보 저장 
		
		
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
	
	
	//질문 정보 수정 
	public void modify(Question question, String subject, String content) {
		
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        
        this.questionRepository.save(question);  //DB에 UPDATE
        
    }

	//질문 삭제 
	public void delete(Question question) {
		
        this.questionRepository.delete(question); //DB에 DELETE
    }

}








