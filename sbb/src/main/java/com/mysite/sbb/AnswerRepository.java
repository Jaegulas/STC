package com.mysite.sbb;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * [AnswerRepository = answer 테이블 전용 DB 작업 인터페이스]
 *
 * ── 제네릭 의미 ────────────────────────────────────────────────────
 *   JpaRepository<Answer, Integer>
 *     · 첫 번째 Answer  : 다룰 엔티티 (answer 테이블)
 *     · 두 번째 Integer : 기본키(Answer.id) 타입
 *
 * ── 구현 클래스가 없는데 어떻게 동작하나? ────────────────────────────
 *   스프링 데이터 JPA 가 프로그램 시작 시 이 인터페이스의 "구현 객체"를 자동으로 만들어 줍니다.
 *   그래서 AnswerService 에서 answerRepository.save(...) 처럼 바로 호출할 수 있습니다.
 *   이 객체 안에서 EntityManager(JPA 실행 도구)가 SQL 을 만들고 실행합니다.
 *
 * ── 상속받은 메서드 → 실제로 실행되는 SQL (이 프로젝트 실행 로그 기준) ─────
 *
 *   save(answer)  : 저장 또는 수정
 *     · id 가 null  → INSERT  (새 답변 등록)
 *         insert into answer (author_id, content, create_date, modify_date, question_id, id)
 *         values (?, ?, ?, ?, ?, default)
 *     · id 가 있음  → UPDATE  (수정·추천 등 기존 객체 변경)
 *         update answer set author_id=?, content=?, create_date=?, modify_date=?, question_id=? where id=?
 *       ※ 추천은 answer 자체가 아니라 voter 목록이 바뀐 것이므로 answer_voter 에 INSERT 가 나갑니다.
 *
 *   delete(answer): 삭제
 *       delete from answer_voter where answer_id=?   ← 추천 연결 행을 먼저 지움
 *       delete from answer where id=?
 *
 *   findById(id)  : 기본키로 한 건 조회 → Optional<Answer> 반환
 *       select a1_0.id, a2_0.id, a2_0.email, ..., q1_0.id, a3_0.id, ...
 *       from answer a1_0
 *       left join site_user a2_0 on a2_0.id=a1_0.author_id
 *       left join question q1_0  on q1_0.id=a1_0.question_id
 *       left join site_user a3_0 on a3_0.id=q1_0.author_id
 *       where a1_0.id=?
 *       ※ Answer.author, Answer.question 이 EAGER(기본값) 이므로 연관 테이블까지 한 번에 JOIN 됩니다.
 *
 *   count()       : 전체 개수 → select count(*) from answer
 *   findAll()     : 전체 목록 → select ... from answer
 */
public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	// 이 인터페이스 안에 직접 적은 메서드는 없습니다.
	// 필요한 기능은 위에서 설명한 상속 메서드로 충분하기 때문입니다.
}









