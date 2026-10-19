package com.mysite.sbb.question;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/* ==========================================================================
* [QuestionForm 클래스 = 질문 등록/수정 화면의 입력값을 담는 DTO(데이터 전달 객체)]
* ==========================================================================
* 
*  이 클래스는 무엇을 하나?
*   1) 질문 입력 화면(question_form.html)의 제목과 내용을 자바 객체로 받아 줍니다. (데이터 바인딩)
*   2) 제목과 내용이 규칙에 맞는지 검사합니다. (입력 검사: @NotEmpty, @Size)
*/
@Getter
@Setter
public class QuestionForm {
	
	// @NotEmpty : 빈 값이면 "제목은 필수항목입니다." 를 보여 줍니다.
	@NotEmpty(message = "제목은 필수항목입니다.")
	// @Size(max=200) : 최대 200 글자까지 허용합니다. Question.subject 의 varchar(200) 과 같은 기준입니다.
	//   이 값을 넘으면 "크기가 0에서 200 사이여야 합니다" 가 나옵니다. (최소 길이는 지정하지 않아 기본값 0)
	@Size(max=200)
	private String subject;  //질문 제목

	// 내용 입력값입니다. (DB 열 question.content 의 원본, 마크다운 문법 글)
	//   @NotEmpty : 빈 값이면 "내용은 필수항목입니다." 를 보여 줍니다.
	//   길이 제한은 없습니다. DB 열이 TEXT 타입이라 긴 글도 저장할 수 있습니다.
	@NotEmpty(message = "내용은 필수항목입니다.")
	private String content;  //질문 내용
	
}






