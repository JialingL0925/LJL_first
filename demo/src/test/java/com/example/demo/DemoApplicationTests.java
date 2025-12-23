package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testLoginSuccess() throws Exception {
		// 完全沿用你最初的接口路径：/user/login（不换！）
		MvcResult result = mockMvc.perform(post("/user/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("phone", "13800138000")  // 你的测试手机号
						.param("password", "123456"))   // 你的测试密码
				.andReturn();

		// 打印调试信息
		System.out.println("响应状态码：" + result.getResponse().getStatus());
		System.out.println("响应体：" + result.getResponse().getContentAsString());
		System.out.println("请求异常：" + result.getResolvedException());

		// 断言状态码为200
		assertEquals(200, result.getResponse().getStatus());
	}
}