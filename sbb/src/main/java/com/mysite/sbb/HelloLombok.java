package com.mysite.sbb;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

//final 예약어가 붙은 상수메모리값을 초기화 할 생성자 자동생성
@RequiredArgsConstructor

@Getter
@Setter
public class HelloLombok {
    private String hello; 
    private int lombok;  

    public static void main(String[] args) {
        HelloLombok helloLombok = new HelloLombok();
        helloLombok.setHello("헬로");
        helloLombok.setLombok(5);

        System.out.println(helloLombok.getHello());
        System.out.println(helloLombok.getLombok());
    }
}
