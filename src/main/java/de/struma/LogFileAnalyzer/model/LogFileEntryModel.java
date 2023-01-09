package de.struma.LogFileAnalyzer.model;

import lombok.*;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class LogFileEntryModel {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	private Timestamp dateTime;
	private String statusError;
	private String problemClass;
	private String activationProcess;
	private String application;
	private @Column(length = 5_000) String message;

}
