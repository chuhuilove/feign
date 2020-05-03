package com.chuhui.feign.controller;

import org.springframework.web.bind.annotation.*;

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

	@PostMapping("/reqPost")
	public CusClass reqPost() {
		CusClass cusClass = new CusClass();

		cusClass.setAge(25);
		cusClass.setName("cyzi");

		return cusClass;
	}

	@PostMapping("/testPostRequest")
	public CusClass testPostRequest(CusClass originalParam) {
		CusClass cusClass = new CusClass();

		cusClass.setName("--->" + UUID.randomUUID().toString());
		cusClass.setAge(120);
		System.err.println("running.....testPostRequest");
		return cusClass;
	}

	public static class CusClass {

		private String name;
		private Integer age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}

}
