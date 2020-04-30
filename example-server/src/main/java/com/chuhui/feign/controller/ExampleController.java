package com.chuhui.feign.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @AUTHOR: cyzi
 * @DATE: 2020/4/30
 * @DESCRIPTION: todo
 */
@RestController
@RequestMapping("example")
public class ExampleController {

    @GetMapping("/uuid")
    public String getUUid() {

        System.err.println("有人来调用我啦......");

        return UUID.randomUUID().toString();
    }

}
