package com.kwanghoon.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model) {
        model.addAttribute("data", "hello~~");
        return "hello"; // resource.templete 에 있는 hello.html 찾아서 실행
    }

}
