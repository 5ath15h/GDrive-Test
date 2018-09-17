package com.drivetest.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

public interface DriveService {
	
	public HashMap<String, String> listFiles() throws IOException, GeneralSecurityException;
	
	public HashMap<String, String> listFolderFiles(String folderId) throws IOException, GeneralSecurityException;
	
	public void downloadFiles(HttpServletResponse response, String fileId) throws GeneralSecurityException, IOException;
	
	public void exportDownloadFiles(HttpServletResponse response, String fileId) throws GeneralSecurityException, IOException;
	
	public void uploadFiles(MultipartFile file) throws GeneralSecurityException, IOException;
	
	public void uploadToFolder(String folderId, MultipartFile file) throws IOException, GeneralSecurityException;

}
