package com.skilal.flex_employ.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String index() {
        return "灵活用工平台后端服务运行正常！请使用前端地址访问：http://localhost:5173/";
    }

    @GetMapping("/api")
    public String apiIndex() {
        return "API 服务正常运行";
    }
}
