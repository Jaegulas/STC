package com.mysite.sbb;

//commonmark 외부 라이브러리의 Parser 클래스 : 마크 다운언의 코드를 HTML의 Node(문서 구조)로 바꿉니다.
import org.commonmark.parser.Parser;

//commonmark 외부 라이브러리의 Node 클래스: 마크다운 언어을 분석한 "문서 구조"를 담는 자료형입니다.
import org.commonmark.node.Node;

//commonmark 외부 라이브러리의 HtmlRenderer 클래스: Node(문서 구조)를 HTML 문자열로 바꿉니다.
import org.commonmark.renderer.html.HtmlRenderer;

//SpringFramework 에서 제공하는 @Component : 이 클래스의 객체(빈)을 자동생성해서 스프링 컨테이너에 보관 하는 역할.
import org.springframework.stereotype.Component;

//@Component : 어노테이션에 의해 class CommonUtil 의 객체(빈)을 자동생성해서 스프링 컨테이너(톰캣 서버 메모리)에 보관해서 공유합니다.
@Component
public class CommonUtil { 
	
	//markdown 메서드는 마크다운 텍스트를 HTML 문서로 변환하여 리턴한다. 
	//즉, 마크다운 문법이 적용된 일반 텍스트를 변환된 HTML로 리턴한다.
	// ============================================================
    // 메서드 이름: markdown
    //
    // [매개변수]  String markdown
    //         호출하는 쪽이 넘겨주는 마크다운 문자열입니다.
    //         예) "**안녕**"
    //
    // [반환]  String (HTML 문자열)
    //         메서드가 호출한 곳으로 돌려주는 값의 자료형은 String입니다.
    //         값의 내용은 HTML 문자열입니다.
    //         예) "<p><strong>안녕</strong></p>\n"
    //
    //         반환되는 값을 담는 변수는 메서드 안에 따로 없고,
    //         마지막 줄 return renderer.render(document); 의 결과가 그대로 돌아갑니다.
    // ============================================================
	public String markdown(String markdown) {
		
		// [중간 객체 1] Parser
        // 마크다운 글자를 읽는 도구 객체입니다. 반환 값이 아닙니다.
        Parser parser = Parser.builder().build();
        
        // [중간 객체 2] Node document
        // parser.parse(markdown)의 결과를 담는 변수입니다.
        // 마크다운을 분석한 "문서 구조" 객체이며, 가장 바깥쪽 노드입니다.
        // 이 객체도 반환 값이 아닙니다. HTML로 바꾸기 위한 중간 재료입니다.
        Node document = parser.parse(markdown);
               
        // [중간 객체 3] HtmlRenderer renderer
        // Node를 HTML로 바꾸는 도구 객체입니다. 반환 값이 아닙니다.
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        
        // [최종 반환 객체] String (HTML 문자열)
        // renderer.render(document)가 HTML 문자열을 만들어 돌려줍니다.
        // 이 문자열이 markdown() 메서드의 반환 값입니다.
        return renderer.render(document);
    }

}




