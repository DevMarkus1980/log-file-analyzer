package de.struma.LogFileAnalyzer.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
public class LogFileModel {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String application;
    private String path;
    private String fileName;
    private Long fileSize;
    @DateTimeFormat(pattern = "YYYY-MM-DD HH:mm:ss")
    private LocalDateTime lastModified;
    @DateTimeFormat(pattern = "YYYY-MM-DD HH:mm:ss")
    private LocalDateTime lastAccessTime;
    @DateTimeFormat(pattern = "YYYY-MM-DD HH:mm:ss")
    private LocalDateTime createdDate;
    private Boolean activateTracking = false;

    public void setFileSize(Long byteToMB){
        this.fileSize = byteToMB/1024;
    }
}
