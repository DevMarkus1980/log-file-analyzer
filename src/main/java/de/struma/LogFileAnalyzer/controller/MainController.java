package de.struma.LogFileAnalyzer.controller;

import de.struma.LogFileAnalyzer.service.ControllerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping(value = {"/home", "/"})
@Controller
public class MainController {

	ControllerService controllerService;

	public MainController(ControllerService controllerService){
		this.controllerService = controllerService;
	}

	@GetMapping
	public String showHome(Model model) {

		model.addAttribute("app", "all");
		if(controllerService.isFirstInitOnServer()){
			return "redirect:/cleanup/config/";
		}

		controllerService.reloadLogFileRepositoryAndConfigLogFilePathRepositoryIfEmpty(model);
		model.addAttribute("chartData", controllerService.getMainChartData());
		return "Sides/Home/home";
	}
	

}
