package com.immortalcrab.as400.webserver;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController
{
    @RequestMapping("/")
    String hellow()
    {
        return "Hello World!";
    }
}
