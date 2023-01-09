package de.struma.LogFileAnalyzer.controller;

import de.struma.LogFileAnalyzer.model.AdvancedConfigModel;
import de.struma.LogFileAnalyzer.service.ControllerService;
import de.struma.LogFileAnalyzer.service.FileReaderService;
import de.struma.LogFileAnalyzer.utility.LogFileListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping(value = {"/cleanup"})
@Controller
public class ConfigController {

	FileReaderService fileReaderService;
	ControllerService controllerService;
	public ConfigController(FileReaderService fileReaderService, ControllerService controllerService){
		this.fileReaderService =fileReaderService;
		this.controllerService = controllerService;
	}

	@GetMapping(value = {"/showAll"})
	public String showLog(Model model) {

		model.addAttribute("filesOnServer",
				fileReaderService.findAllLogFilesOnServer());
		return "Sides/Config/cleanUp";
	}

	@GetMapping(value = {"/config/"})
	public String showConfig(Model model) {

		LogFileListDTO setDTOFiles = new LogFileListDTO();

		if(controllerService.isFirstInitOnServer()){
			controllerService.firstInitOfLogFilesOnServer();
			model.addAttribute("view","first_setup");
			model.addAttribute("path","/cleanup/config/first/");
		}
		else{
			model.addAttribute("path","/cleanup/config/");
		}

		controllerService.getSelectedLogFilesList().iterator().forEachRemaining(setDTOFiles ::addFile);

		model.addAttribute("form", setDTOFiles);

		return "Sides/Config/config";
	}

	@PostMapping(value = {"/config/"})
	public String saveHome(
			@ModelAttribute LogFileListDTO form, Model model) {

		controllerService.saveAllAfterFirstInit(form.getSetDTOFiles());
		fileReaderService.initAllTrackedLogEntries();
		model.addAttribute("form", controllerService.getSelectedLogFilesList());
		return "redirect:/cleanup/config/";
	}

	@PostMapping(value = {"/config/first/"})
	public String saveByFirstInit(
			@ModelAttribute LogFileListDTO form, Model model) {

		controllerService.saveAllAfterFirstInit(form.getSetDTOFiles());
		return "redirect:/";
	}

	@GetMapping(value = {"/advanced_config/"})
	public String showAdvisedConfig(Model model) {

		model.addAttribute("advancedConfig", controllerService.getAdvisedConfig());
		return "Sides/Config/advancedConfig";
	}

	@PostMapping(value = {"/advanced_config/"})
	public String saveAdvisedConfig(
			@ModelAttribute AdvancedConfigModel advancedConfig, Model model) {

		if(controllerService.saveAdvancedConfigAndCheckIfConfigChanged(advancedConfig))
			controllerService.saveAdvisedConfig(advancedConfig);

		return "redirect:/cleanup/advanced_config/";
	}

}
