package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
@RequestMapping("/user")
public class UserController {

	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		
		String userName=principal.getName();
		
		//get the userName using email
		System.out.println("USER_NAME "+userName);
		
		User user=userRepository.getUserByUserName(userName);
		
		System.out.println("USER" +user);
		
		model.addAttribute("user",user);
		
	}
	
	
	
	//open user_dashboard handler
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");

	
		return "normal/user_dashboard";
	}
	
	
	//open add_form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//process-contact
	@PostMapping("/process-contact")
	public String ProcessContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal) {
		
		try {
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		
		//processing of uploading image
		if (file.isEmpty()) {
			
			contact.setImage("contact.png");
			System.out.println("Image field is empty");
		}
		
		else {
			contact.setImage(file.getOriginalFilename());
			System.out.println("1");
			File saveFile=new ClassPathResource("static/img").getFile();
			System.out.println("2");
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			System.out.println("3");
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("image added Successfully");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("Data "+contact);
		
		System.out.println("added Succesfully");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error message "+e.getMessage());
			e.printStackTrace();
		}
		return "normal/add_contact_form";
		
		}
	
	
	//Show Contact handle
	@GetMapping("/show-contacts/{page}")
	String showContact(@PathVariable("page") Integer page,Model m, Principal principal) {
		m.addAttribute("Title", "Contact View Page");
		
		//contact ki list ko Send karna hai
		//one way to retrive the contact from userRepository other wise u can create separate contact repository
		/*
		 * String UserName=principal.getName(); User
		 * user=this.userRepository.getUserByUserName(UserName); List<Contact>
		 * contact=user.getContacts();
		 */
		
		String UserName=principal.getName();
		User user=this.userRepository.getUserByUserName(UserName);
		
		PageRequest pageable=PageRequest.of(page, 1);
		
		Page<Contact> contact=this.contactRepository.findContactByUser(user.getId(),pageable );
		m.addAttribute("contact", contact);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contact.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	//perticular contact detail
	@RequestMapping("/{cid}/contact")
	public String showView(@PathVariable("cid" ) Integer cid, Model m, Principal principal) {
		
		
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cid);
		
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		m.addAttribute("contact", contact);
		m.addAttribute("Title", contact.getName());
		}
		System.out.println("CID" +cid);
		return "normal/contact_detail";
	}
	
    //delete contact id
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session, Principal principal){
		
		Optional<Contact> optional=this.contactRepository.findById(cid);
		Contact contact=optional.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		
		
		if (user.getId()==contact.getUser().getId()) {
			
			//in user we set the maping cascad.all so that we use this trick first unlink the corrossponded contact from the user table then we delete 
			
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			//delete image(referance from delete operation update process handler )
			 
			try {
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,contact.getImage());
				file1.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			session.setAttribute("Message", new Message("Contact deleted successful","success"));
		}
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//update the contact
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		
		
		
		m.addAttribute("title", "Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	
	//update contact handler
	
	@RequestMapping(value="/process-update", method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, HttpSession session, Principal principal) {
		
		
		try {
			//old contactdetails
			Contact oldContact=this.contactRepository.findById(contact.getCid()).get();
			
			
			if (!file.isEmpty()) {
				
				//delete photo 
				
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContact.getImage());
				file1.delete();
				
				
				//update photo
				
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				System.out.println("image update Successfully");
					
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		session.setAttribute("message", new Message("your contact is updated.............", "Sucess"));
		
		System.out.println("Contact Id :"+contact.getCid());
		System.out.println("Contact Name :"+contact.getName());
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		
		m.addAttribute("Title","your Profile");
		
		return "normal/profile";
	}
	
	// open Settings handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		
		System.out.println("old Password "+oldPassword);
		System.out.println("new Password "+newPassword);

		
	    String userName=principal.getName();
	    
	    User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("your password is updated.............", "Sucess"));
			return "redirect:/user/index";
		}else {
			session.setAttribute("message", new Message("wrong old password.............", "Sucess"));
			return "redirect:/user/settings";
		}
	    
		
		
	}
	
}