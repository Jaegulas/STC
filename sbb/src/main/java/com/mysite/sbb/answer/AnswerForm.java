package com.mysite.sbb.answer;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/*
 * ==========================================================================
 * [AnswerForm 클래스 = 답변 입력 화면의 값을 담는 DTO(데이터 전달 객체)]
 * ==========================================================================
 *
 * 이 클래스는 무엇을 하나?
 *   1) 답변 입력칸의 글을 자바 객체로 받아 줍니다. (데이터 바인딩)
 *   2) 글이 비어 있지 않은지 검사합니다. (입력 검사, @NotEmpty)
 *
 * 이 클래스가 하지 않는 일
 *   - DB 와 연결되지 않습니다. @Entity 가 없으므로 SQL 을 만들지 않습니다.
 *   - 답변을 저장하지 않습니다. 저장은 AnswerService.create / modify 가 합니다.
 *
 * ── 이 클래스가 쓰이는 두 화면과 두 주소 ────────────────────────────────────
 *
 *   [1] 질문 상세 화면 하단의 답변 입력칸 (question_detail.html)
 *       <form th:action="@{|/answer/create/${question.id}|}" th:object="${answerForm}" method="post">
 *         <textarea th:field="*{content}" ...>
 *       → 제출 주소 : POST /answer/create/{질문번호}  → AnswerController.createAnswer()
 *
 *   [2] 답변 수정 화면 (answer_form.html)
 *       <form th:object="${answerForm}" method="post">
 *         <textarea th:field="*{content}" ...>
 *       → GET  /answer/modify/{답변번호} 로 화면을 열 때 기존 내용이 채워짐 (AnswerController.answerModify, GET)
 *       → POST /answer/modify/{답변번호} 로 저장 (AnswerController.answerModify, POST)
 *
 * ── 요청 값이 이 객체로 들어가는 흐름 ─────────────────────────────────────
 *
 *   [브라우저] 글 입력 후 제출
 *     → 요청 파라미터 "content=입력한글" 이 전송됨
 *     → [스프링 데이터 바인딩] "content" 값 → setContent("입력한글") 호출
 *     → [Bean Validation (@Valid)] @NotEmpty 검사
 *     → [BindingResult] 검사 결과를 담음
 *     → [컨트롤러 메서드 본문] hasErrors() 로 성공/실패를 나눠 처리
 *
 *   화면에 다시 출력할 때는 반대로 getContent() 가 호출되어 입력칸에 값이 채워집니다.
 *   (검사에 실패해서 같은 화면을 다시 보여 줄 때 입력했던 글이 그대로 남는 이유입니다)
 *
 * ── 애너테이션 역할 ───────────────────────────────────────────────────
 *
 *   @Getter
 *     - getContent() 를 Lombok 이 자동으로 만듭니다.
 *     - 화면 출력(${answerForm.content}, th:field)과 컨트롤러의 값 꺼내기(answerForm.getContent())에 쓰입니다.
 *
 *   @Setter
 *     - setContent(String) 를 Lombok 이 자동으로 만듭니다.
 *     - 데이터 바인딩이 요청 값을 넣을 때 이 메서드를 호출합니다.
 *
 *   @NotEmpty(message = "내용은 필수항목입니다.")
 *     - 값이 null 이거나 빈 문자열("")이면 검사에 실패합니다.
 *     - 실패하면 BindingResult 에 오류가 담기고, 컨트롤러는 저장을 하지 않습니다. (INSERT/UPDATE 없음)
 *     - message 값이 form_errors.html 을 통해 화면에 그대로 출력됩니다.
 *     - 실제 확인: 빈 내용으로 등록하면 "내용은 필수항목입니다." 가 나오고, 답변은 저장되지 않았습니다.
 *     - 공백만 입력한 " " 은 통과합니다. (글자 수가 1이므로 "비어 있음"이 아니라고 봅니다)
 *       공백도 막으려면 @NotBlank 를 씁니다.
 */

@Getter
@Setter
public class AnswerForm { //DB에 등록하기 위해 입력한 답변 내용 검사  +   데이터 전달  역할의 DTO
	
	 @NotEmpty(message = "내용은 필수항목입니다.")
	 private String content;
	 
}













