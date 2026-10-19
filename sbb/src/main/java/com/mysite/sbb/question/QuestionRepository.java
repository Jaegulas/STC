package com.mysite.sbb.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*
 * ==========================================================================
 * [QuestionRepository 인터페이스 = question 테이블 전용 DB 작업 창구]
 * ==========================================================================
 *
 * 구현 코드가 없는데 동작하는 이유
 *   - 스프링 데이터 JPA 가 프로그램 시작 때 이 인터페이스의 구현 객체를 자동으로 만듭니다.
 *   - 그 객체는 Bean 으로 등록되고, QuestionService 가 생성자 주입으로 받아 씁니다.
 *   - 메서드를 세 가지 방법으로 만들 수 있고, 각 방법마다 SQL 을 만드는 원리가 다릅니다.
 *
 * ── 메서드를 만드는 세 가지 방법 ──────────────────────────────────────────────
 *   1) 상속 메서드    : JpaRepository 가 이미 가진 save, delete, findById, count 등. 이름을 적지 않아도 됩니다.
 *   2) 쿼리 메서드    : 메서드 이름을 규칙대로 지으면 SQL 이 자동 생성됩니다. 예) findBySubject
 *   3) @Query 메서드  : SQL 과 비슷한 JPQL 을 직접 적습니다. 예) findAllByKeyword
 *
 * ── 호출 관계 (이 프로젝트 기준) ─────────────────────────────────────────────
 *   QuestionController
 *     → QuestionService         (생성자 주입)
 *       → QuestionRepository    (이 인터페이스, 구현 객체는 스프링이 만듦)
 *         → EntityManager → SQL 생성 → JDBC → H2 DB (question 테이블)
 *
 * ── 상속 메서드가 실제로 실행하는 SQL (이 프로젝트 실행 로그 확인값) ─────────────
 *
 *   save(질문), id 없음 → INSERT   : 질문 등록 (QuestionService.create)
 *       insert into question (author_id, content, create_date, modify_date, subject, id)
 *       values (?, ?, ?, ?, ?, default)
 *
 *   save(질문), id 있음 → UPDATE   : 질문 수정 (QuestionService.modify), 추천 (QuestionService.vote)
 *       update question set author_id=?, content=?, create_date=?, modify_date=?, subject=? where id=?
 *       ※ 추천의 실제 INSERT 는 question_voter 테이블에 나갑니다. (아래 vote 설명 참고)
 *
 *   delete(질문) : 질문 삭제 (QuestionService.delete)
 *       delete from question_voter where question_id=?      ← 추천 행
 *       delete from answer_voter   where answer_id=?        ← 답변마다, 답변의 추천 행
 *       delete from answer         where id=?              ← 답변마다
 *       delete from question       where id=?              ← 마지막으로 질문
 *       ※ 답변과 추천 행이 먼저 지워지는 이유: 외래키 제약. Question.java 의 answerList 설명 참고.
 *
 *   findById(id) : 질문 한 건 조회 (QuestionService.getQuestion)
 *       select q1_0.id, a1_0.id, a1_0.email, a1_0.password, a1_0.username,
 *              q1_0.content, q1_0.create_date, q1_0.modify_date, q1_0.subject
 *       from question q1_0
 *       left join site_user a1_0 on a1_0.id=q1_0.author_id
 *       where q1_0.id=?
 *       ※ 질문의 작성자(author)가 EAGER 기본값이라 site_user 가 JOIN 됩니다.
 *
 *   count() : 전체 질문 개수 → select count(*) from question   (이 프로젝트에서는 호출하지 않음)
 *
 * ── 이 인터페이스를 쓰는 주요 흐름 한눈에 보기 ──────────────────────────────────
 *   질문 목록   GET  /question/list      → findAllByKeyword (검색 + 정렬 + 페이징)
 *   질문 상세   GET  /question/detail/{id} → findById
 *   질문 등록   POST /question/create    → save(INSERT)
 *   질문 수정   POST /question/modify/{id} → findById, save(UPDATE)
 *   질문 추천   GET  /question/vote/{id} → findById, save(INSERT question_voter)
 *   질문 삭제   GET  /question/delete/{id} → findById, delete(연쇄 삭제)
 */
public interface QuestionRepository extends JpaRepository<Question, Integer> {

	/*
	 * ──────────────────────────────────────────────────────────────────────────
	 * [2) 쿼리 메서드] 이름 규칙으로 SQL 이 만들어지는 방식
	 * ──────────────────────────────────────────────────────────────────────────
	 *   문법: find + (엔티티명) + By + 변수명 [+ And + 변수명] [+ Like]
	 *   스프링 데이터가 메서드 이름을 나누어 읽습니다.
	 *     find    → 조회 (select)
	 *     By      → WHERE 절 시작
	 *     Subject → subject 변수(열)를 조건으로 씀
	 *     And     → 조건을 하나 더 붙임
	 *     Like    → 포함 검색 (LIKE)
	 *
	 * 아래 3개는 이 프로젝트의 서비스 코드에서 호출하지 않습니다. 테스트 예제(SbbApplicationTests)에서만 쓰입니다.
	 * 그래서 SQL 은 이름 규칙으로 결정되는 형태로 적었고, 실행 로그로 확인한 것은 아닙니다.
	 */

	// select ... from question q1_0 where q1_0.subject=?   → 결과가 1건이라고 가정
	Question findBySubject(String subject);
	
	// select ... from question q1_0 where q1_0.subject=? and q1_0.content=? → 결과가 1건이라고 가정
	Question findBySubjectAndContent(String subject, String content);
	
	// select ... from question q1_0 where q1_0.subject like ?   → 'sbb%' 같은 패턴으로 여러 건을 찾음
	List<Question> findBySubjectLike(String subject);
	
	
	
	/* --------------------------------------------------------------------------------
	 * [findAll(Pageable)] 하단 페이지번호를 클릭하여 요청한 "페이지 번호 단위"로 나누어 질문 레코드를 조회 하는 메소드
	 * --------------------------------------------------------------------------------
	 *
	 * 매개변수로 받는 값 : Pageable pageable
	 *   "몇 번째 페이지를, 한 페이지에 몇 개씩, 어떤 정렬 순서로" 조회해서 레코드드를 가져올지 적은 요청 정보들이 저장되는 Pageable인터페이스의 구현객체입니다.
	 *   Pageable 은 인터페이스이므로, 실제로는 PageRequest.of(...) 로 만든  Pageable인터페이스의 구현한 반환 자식객체를 받습니다.
	 *
	 * 반환값 : Page<Question>
	 *   요청한(클릭한) 페이지 번호 화면에 해당하는 DB에서 조회된 질문 레코드 목록과, 페이지 정보(조회된 체 질문 레코드수, 전체 페이지번호 개수 등)를 함께 담은 객체입니다.
	 *
	 * ------------------------------------------------------------
	 * [이 메서드와 관련된 세 클래스]
	 * ------------------------------------------------------------
	 *
	 * 1) org.springframework.data.domain.Pageable (인터페이스)
	 * 
	 *    클릭한 페이지 요청 정보들이 보관되어 제공하는 메소드들이 정의된 인터페이스 입니다.
	 *    아래 정보를 꺼내 쓸 수 있는 메서드를 정의합니다.
	 *      getPageNumber() : 요청한 페이지 번호 (0 부터 시작)
	 *      getPageSize()   : 한 페이지에 가져올 데이터 개수
	 *      getOffset()     : 건너뛸 데이터 개수 = 페이지 번호 × 페이지 크기
	 *      getSort()       : 정렬 조건
	 *    인터페이스이므로 new Pageable() 로 직접 만들 수 없고, 구현 자식클래스를 통해 만듭니다.
	 *
	 * 2) org.springframework.data.domain.PageRequest (Pageable인터페이스를 구현한 자식 클래스)
	 *    Pageable 인터페이스를 실제로 구현한 자식클래스입니다.
	 *
	 *    클릭해서 요청한 현재 페이지 번호와 한 페이지에 보여줄 조회된 레코드 개수 등을 설정해 페이징 요청정보를 만드는 역할을 합니다.
	 *    
	 *    생성 방법: PageRequest.of(클릭해서 요청한 페이지번호, 한 페이지에 보여줄 조회된 레코드 개수, 정렬조건)
	 *    
	 *      예1) PageRequest.of(0, 10)                        → 0(1p) 페이지, 10개, 정렬 없음
	 *      
	 *      예2) PageRequest.of(1, 10, Sort.by("createDate").descending())
	 *                                                        → 0(1p) 페이지, 10개, 작성일 내림차순
	 *    QuestionService.getList() 에서 이 클래스로 Pageable 을 만들어 이 메서드에 넘깁니다.
	 *
	 * 3) org.springframework.data.domain.Page (인터페이스)
	 * 
	 *    조회 결과를 담는 자식 구현 클래스의 부모인터페이스 입니다. 이 메서드의 반환 타입입니다.
	 *    Spring 이 DB 조회 후 실제 객체를 만들어 돌려주므로, 개발자가 new Page() 로 만들지 않습니다.
	 *    주요 메서드:
	 *      getContent()        : 이번 페이지의 데이터 목록 (List<Question>)
	 *      getTotalElements()  : 전체 데이터 개수
	 *      getTotalPages()     : 전체 페이지 수
	 *      getNumber()         : 현재 페이지 번호 (0 부터 시작)
	 *      hasPrevious()       : 이전 페이지가 있는지 여부
	 *      hasNext()           : 다음 페이지가 있는지 여부
	 *
	 * ------------------------------------------------------------
	 * [실행 흐름]
	 * ------------------------------------------------------------
	 *
	 *   QuestionService.getList(page)
	 *     → PageRequest.of(page, 10, 정렬조건)  : Pageable 객체 생성 (아직 DB 조회 안 함)
	 *     → findAll(pageable)                    : 이 메서드 호출, DB 에 SELECT 질의 전송
	 *     → DB 가 해당 범위의 질문 10개를 반환
	 *     → 전체 개수를 세는 COUNT 질의 실행
	 *     → Page<Question> 객체를 만들어 QuestionService 에 반환
	 *
	 */
	Page<Question> findAll(Pageable pageable);
	
	
}












