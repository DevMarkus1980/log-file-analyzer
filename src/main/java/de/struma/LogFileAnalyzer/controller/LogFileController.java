package de.struma.LogFileAnalyzer.controller;

import de.struma.LogFileAnalyzer.service.ControllerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class LogFileController {

	ControllerService controllerService;

	public LogFileController (ControllerService controllerService){
		this.controllerService = controllerService;
	}

	@GetMapping(value = {"/logs/logs_all"})
	public String showLog(Model model) {
		return controllerService.getAllLogs(model);
	}

	@GetMapping(value = {"/logs/logs_reload"})
	public String reloadAllLogs(
			@RequestParam(value = "application", defaultValue = "all") String application,Model model) {
		return controllerService.reloadAllLogs(model, application);
	}

}
