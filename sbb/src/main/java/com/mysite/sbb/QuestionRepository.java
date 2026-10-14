package com.mysite.sbb;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * [QuestionRepository = question 테이블 전용 DB 작업 인터페이스]
 *
 * JpaRepository<Question, Integer> 를 상속하면 구현 없이 아래 기본 기능을 씁니다.
 * 이 인터페이스에 메서드를 "이름 규칙"이나 "@Query"로 적으면, 그 메서드에 맞는 SQL 을 스프링이 만들어 줍니다.
 *
 * ── 메서드 종류 3가지 ─────────────────────────────────────────────
 *   1) 상속 메서드    : save, delete, findById, count 등 (이름을 직접 적지 않음)
 *   2) 쿼리 메서드    : 메서드 이름만 규칙대로 지음 → findBySubject 처럼 SQL 자동 생성
 *   3) @Query 메서드  : 직접 JPQL(엔티티 기준 질의문)을 적음 → findAllByKeyword
 *
 * ── 상속 메서드가 실행하는 SQL (실행 로그 확인값) ────────────────────
 *   save(질문, id 없음) → insert into question (author_id, content, create_date, modify_date, subject, id)
 *                         values (?, ?, ?, ?, ?, default)
 *   save(질문, id 있음) → update question set author_id=?, content=?, create_date=?, modify_date=?, subject=? where id=?
 *   delete(질문)        → (cascade 로 답변과 추천 행이 먼저 정리됨. 자세한 순서는 Question.java 참고)
 *                         delete from question_voter where question_id=?
 *                         delete from answer_voter where answer_id=?   ← 답변마다
 *                         delete from answer where id=?                 ← 답변마다
 *                         delete from question where id=?
 *   findById(id)        → select q1_0.id, a1_0.id, a1_0.email, a1_0.password, a1_0.username,
 *                                q1_0.content, q1_0.create_date, q1_0.modify_date, q1_0.subject
 *                         from question q1_0 left join site_user a1_0 on a1_0.id=q1_0.author_id
 *                         where q1_0.id=?
 *   count()             → select count(*) from question
 */
public interface QuestionRepository extends JpaRepository<Question, Integer> {

	/*
	 * [2) 쿼리 메서드] 이름 규칙 문법
	 *   find + (엔티티명) + By + 변수명 [+ And + 변수명] [+ Like]
	 *   Spring Data JPA.jar 가 메서드 이름을 분해해서 WHERE 절을 만듭니다.
	 *
	 * 참고: 쿼리 메서드 이름 규칙에서 (엔티티명) 부분은 생략할 수 있습니다.
	 *
	 * 아래 3개는 이 프로젝트의 서비스 코드에서는 호출하지 않고, 테스트 예제(SbbApplicationTests)에서 사용됩니다.
	 * (SQL 은 이름 규칙으로부터 결정되는 형태이며, 테스트를 실행해야 실제 로그를 볼 수 있습니다)
	 */

	// select ... from question q1_0 where q1_0.subject=?   → 결과가 1건이라고 가정
	Question findBySubject(String subject);
	
	// select ... from question q1_0 where q1_0.subject=? and q1_0.content=? → 결과가 1건이라고 가정
	Question findBySubjectAndContent(String subject, String content);
	
	// select ... from question q1_0 where q1_0.subject like ?   → 'sbb%' 같은 패턴으로 여러 건을 찾음
	List<Question> findBySubjectLike(String subject);
	
	
}












