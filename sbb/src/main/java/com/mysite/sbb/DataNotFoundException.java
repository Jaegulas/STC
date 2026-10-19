package com.mysite.sbb;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//DataNotFoundException은 데이터베이스에서 특정 엔티티 또는 데이터를 찾을 수 없을 때 발생시키는 예외 클래스로 만들었다. 
//이 예외가 발생하면 스프링 부트는 설정된 HTTP 상태 코드(HttpStatus.NOT_FOUND)와 이유("entity not found")를 포함한 응답을 생성하여 
//클라이언트에게 반환하게 된다. 여기서는 404 오류를 반환하도록 작성했다.

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class DataNotFoundException extends RuntimeException {
	
	//RuntimeException 클래스를 상속하는 것은 사용자 정의 예외 클래스를 정의하는 방법 중 하나이다. 
	//RuntimeException은 실행 시 발생하는 예외라는 의미이다
	
	
    private static final long serialVersionUID = 1L;
    
    public DataNotFoundException(String message) {
        super(message);
    }
}
