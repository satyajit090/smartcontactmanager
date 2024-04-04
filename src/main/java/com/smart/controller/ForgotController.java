package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotController {

	Random random=new Random(1000);
	
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "Forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email) {
		
		System.out.println("Email ID: "+email);
		
		////generating otp 4 digit
		
		
		int otp=random.nextInt(99999999);
		
		System.out.println("OTP "+otp);
		return "verify-otp";
	}
}
