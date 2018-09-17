package com.drivetest.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;

@Service
public class DriveServiceImpl implements DriveService {

	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

		// Load client secrets.
		InputStream in = DriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public Drive getService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}

	@Override
	public HashMap<String, String> listFiles() throws IOException, GeneralSecurityException {

		HashMap<String, String> fileNames = new HashMap<>();

		Drive service = getService();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
				fileNames.put(file.getName(), file.getId());
			}
		}

		return fileNames;
	}

	@Override
	public HashMap<String, String> listFolderFiles(String folderId) throws IOException, GeneralSecurityException {

		HashMap<String, String> fileNames = new HashMap<>();
		Drive service = getService();

		FileList result = service.files().list().setQ("'" + folderId + "' in parents").execute();
		List<File> files = result.getFiles();
		System.out.println("Files:");
		for (File file : files) {
			System.out.printf("%s (%s)\n", file.getName(), file.getId());
			fileNames.put(file.getName(), file.getId());
		}

		return fileNames;
	}

	@Override
	public void downloadFiles(HttpServletResponse response, String fileId) throws GeneralSecurityException, IOException {
		Drive service = getService();
		File file = service.files().get(fileId).setFields("parents, id, name, mimeType, webContentLink,fileExtension").execute();
		// file=service.files().get(fileId).setFields("parents").execute();
		//ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		 //f.export(fileId, file.getMimeType()).executeMediaAndDownloadTo(outputStream);
		//ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		//
		//file= service.files().get(fileId).setAlt("media").execute();
		 HttpResponse resp =
		            service.getRequestFactory().buildGetRequest(new GenericUrl(file.getWebContentLink()))
		                .execute();
		 InputStream inputStream = resp.getContent();
		response.setContentType(file.getMimeType());
		response.setHeader("Content-Disposition", "attachment;filename=" +String.format("attachment; filename=\"" + file.getName()+file.getFileExtension() +"\"") );
		response.setContentLengthLong(file.getSize());
		

		FileCopyUtils.copy(inputStream, response.getOutputStream());

		return;
	}

	@Override
	public void exportDownloadFiles(HttpServletResponse response, String fileId)
			throws GeneralSecurityException, IOException {
		Drive service = getService();
		File file = service.files().get(fileId).setFields("parents, id, name, mimeType, webContentLink").execute();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		service.files().export(fileId,"application/pdf").setFields("parents, id, name, mimeType, webContentLink,fileExtension").executeMediaAndDownloadTo(outputStream);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());

		FileCopyUtils.copy(inputStream, response.getOutputStream());

		return;
	}

	@Override
	public void uploadFiles(MultipartFile file) throws GeneralSecurityException, IOException {
		Drive service = getService();
		File fileMetadata = new File();
		fileMetadata.setName(file.getOriginalFilename());
		fileMetadata.setMimeType(file.getContentType());
		InputStreamContent mediaContent = new InputStreamContent(file.getContentType(),
				new BufferedInputStream(file.getInputStream()));
		service.files().create(fileMetadata, mediaContent).setFields("parents, id, name, mimeType, webContentLink,fileExtension").execute();

	}

	@Override
	public void uploadToFolder(String folderId, MultipartFile file) throws IOException, GeneralSecurityException {
		Drive service = getService();
		File fileMetadata = new File();
		fileMetadata.setName(file.getOriginalFilename());
		fileMetadata.setMimeType(file.getContentType());
		fileMetadata.setParents(Collections.singletonList(folderId));
		InputStreamContent mediaContent = new InputStreamContent(file.getContentType(),
				new BufferedInputStream(file.getInputStream()));
		fileMetadata = service.files().create(fileMetadata, mediaContent).setFields("parents, id, name, mimeType, webContentLink,fileExtension").execute();
		System.out.println("File ID: " + fileMetadata.getId());

	}


}
