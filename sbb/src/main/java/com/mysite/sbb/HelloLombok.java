package com.mysite.sbb;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


//final 예약어가 붙은 상수메모리값을 초기화 할 생성자 자동생성 
@RequiredArgsConstructor

//@Getter, @Setter라는 애너테이션을 적용했더니 Getter와 Setter 메서드를 따로 작성하지 않아도 
//setHello, setLombok, getHello, getLombok 등의 메서드를 사용할 수 있게 되었다.
@Getter
@Setter
public class HelloLombok { 

	private final String hello;
    private final int lombok; 
    /*
    public HelloLombok(String hello, int lombok) { 
        this.hello = hello; 
        this.lombok = lombok; 
    }  
    */
    
    public static void main(String[] args) {
        HelloLombok helloLombok = new HelloLombok("헬로", 5);
      
        System.out.println(helloLombok.getHello());
        System.out.println(helloLombok.getLombok());
    }
}
