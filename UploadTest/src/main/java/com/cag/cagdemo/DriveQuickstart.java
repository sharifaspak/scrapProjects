package com.cag.cagdemo;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveQuickstart {
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
		InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s) (%s) (%s)\n", file.getName(), file.getId(),file.getMimeType(),file.getKind());
			}
		}
		
		//Create folder
		
		File fileMetadata = new File();
		fileMetadata.setName("Invoices");
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		File file = service.files().create(fileMetadata)
		    .setFields("id")
		    .execute();
		System.out.println("Folder ID: " + file.getId());
		
		//save file
		String folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
		File fileMetadata1 = new File();
		fileMetadata1.setName("photo.jpg");
		fileMetadata1.setParents(Collections.singletonList(folderId));
		java.io.File filePath = new java.io.File("/photo.jpg");
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		File file1 = service.files().create(fileMetadata1, mediaContent)
		    .setFields("id, parents")
		    .execute();
		System.out.println("File ID: " + file1.getId());
		
		
		//download file
		
		String fileId = "0BwwA4oUTeiV1UVNwOHItT0xfa2M";
		OutputStream outputStream = new ByteArrayOutputStream();
		service.files().get(fileId)
		    .executeMediaAndDownloadTo(outputStream);
		
		
		//Search for files
		
		String pageToken = null;
		do {
		  FileList result1 = service.files().list()
		      .setQ("mimeType='image/jpeg'")
		      .setSpaces("drive")
		      .setFields("nextPageToken, files(id, name)")
		      .setPageToken(pageToken)
		      .execute();
		  for (File file11 : result1.getFiles()) {
		    System.out.printf("Found file: %s (%s)\n",
		        file11.getName(), file11.getId());
		  }
		  pageToken = result1.getNextPageToken();
		} while (pageToken != null);
	}
}