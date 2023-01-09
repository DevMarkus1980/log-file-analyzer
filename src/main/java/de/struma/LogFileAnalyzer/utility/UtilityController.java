package de.struma.LogFileAnalyzer.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class UtilityController {
	
	@Value("${app.version}")
	private String appVersion;
	
	@Bean(name = "versionsnummer") // Gibt einen String zum Frontend über das Interface FrontService
	public FrontService getVersion() {
		return () -> "Version "+appVersion;
	}
}
interface FrontService{
	String getVersion();
}