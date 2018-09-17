package com.drivetest.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drivetest.service.DriveService;



@RestController
public class DriveController {
	
	@Autowired
	DriveService driveService;
	
	@RequestMapping(value = "/hello", method = RequestMethod.GET,headers="Accept=application/json",produces = "application/json")
	public ResponseEntity<String> hello() {
		return new ResponseEntity<String>("helloWorld",HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/list",method = RequestMethod.GET,headers="Accept=application/json")
	public HashMap<String, String> list() throws IOException, GeneralSecurityException{
		return driveService.listFiles();
	}
	
	@RequestMapping(value="/folderlist",method = RequestMethod.GET,headers="Accept=application/json")
	public HashMap<String, String> folderlist(@RequestParam("folderId") String folderId) throws IOException, GeneralSecurityException{
		return driveService.listFolderFiles(folderId);
	}
	
	@RequestMapping(value="/download",method = RequestMethod.GET,headers="Accept=application/json")
	public void download(HttpServletResponse response, @RequestParam("fileId") String fileId) throws GeneralSecurityException, IOException {
		System.out.println("fileId" + fileId);
		driveService.downloadFiles(response ,fileId);	
	}
	
	@RequestMapping(value="/exportdownload",method = RequestMethod.GET,headers="Accept=application/json")
	public void exportDownload(HttpServletResponse response, @RequestParam("fileId") String fileId) throws GeneralSecurityException, IOException {
		System.out.println("fileId" + fileId);
		driveService.exportDownloadFiles(response ,fileId);	
	}
		
	@RequestMapping(value="/uploadfile",method = RequestMethod.POST,headers=("content-type=multipart/*"))
	public void uploadAPI(@RequestParam("file") MultipartFile file) throws IOException, GeneralSecurityException{ 
		driveService.uploadFiles(file);
		
	}
	
	@RequestMapping(value="/uploadtofolder",method = RequestMethod.POST,headers=("content-type=multipart/*"))
	public void uploadToFolder(@RequestParam("folderId") String folderId,@RequestParam("file") MultipartFile file) throws IOException, GeneralSecurityException{
		 driveService.uploadToFolder(folderId,file);
	}
}
