package org.rb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LocaleController {

	@GetMapping("/")
	public String index() {
		
		return "welcome";
	}
	
	@GetMapping("/locale")
	public String getLocalePage() {
		return "welcome";
	}
}
