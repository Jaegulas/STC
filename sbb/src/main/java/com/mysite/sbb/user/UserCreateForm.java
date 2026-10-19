package com.mysite.sbb.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm { //회원가입 요청페이지에서 입력한 값을 변수에 저장하고 
							  //입려한 값들을 검증해서 전달하는 운반상자(DTO)
	
	@Size(min = 3, max = 25) //username 은 입력받는 데이터의 길이가 3~25 사이여야 한다는 검증 조건을 설정했다. 
							 //@Size는 문자열의 길이가 최소 길이(min)와 최대 길이(max) 사이에 해당하는지를 검증한다.
	@NotEmpty(message ="사용자ID는 필수항목입니다.")
	private String username;
	
	@NotEmpty(message = "비밀번호는 필수항목입니다.")
	private String password1;
	
	@NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
	private String password2;
	
	@NotEmpty(message = "이메일은 필수항목입니다.")
	@Email            // @Email은 해당 속성의 값이 이메일 형식과 일치하는지를 검증한다.
	private String email;
	
}




