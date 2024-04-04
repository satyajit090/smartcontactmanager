package com.smart.controller;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Messages;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;




@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/home")
	public String home(Model model) {
		
		model.addAttribute("title","HOME SMART CONTACT MANAGER_");
			return "home";
		}
	
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","ABOUT- SMART CONTACT MANAGER_");
			return "about";
		}
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","SIGNUP- SMART CONTACT MANAGER_");
		model.addAttribute("user", new User());
		
			return "signup";
		}
	
	
		/* handler for registering user */
		/* @modelAttribute use for match the data form input to store from user table */
		/* @equestParam use for checkbox , this value is not present  in table for that reason we use "@RequestParam(value="agreement", defaultValue="false") boolean agreement" */
		/* for data send to user table we use Model */
	@RequestMapping(value="/do_register",method=RequestMethod.POST )
	  public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1 ,@RequestParam(value="agreement", defaultValue="false") boolean agreement, Model model,HttpSession session) {
	    	
		
	
		try {
			
			if (!agreement) {
				System.out.println("you have not agreed terms and condition");
				throw new Exception("you have not agreed terms and condition");
			}
			
			if (result1.hasErrors()) {
				
				System.out.println("Error" +result1.toString());
				model.addAttribute("user",user);
				return "signup";
				
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("deault.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("agreement"+agreement);
			System.out.println("User" +user);
			
			User result=this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new com.smart.helper.Message("Succes ful register","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new com.smart.helper.Message("something went wrong"+e.getMessage(),"alert-danger"));
			return "signup";
		
		}
	    	
	    	
	    }
	
	
	@RequestMapping("/signin")
	public String Customlogin(Model model) {
		
		model.addAttribute("title","LOGIN- SMART CONTACT MANAGER_");
			return "login";
		}
	
	
	}


