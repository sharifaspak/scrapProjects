package com.cag.cagdemo;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class MainController {
	private FileManager fileManager;

	@GetMapping({"/"})
	public ResponseEntity<List<File>> listEverything() {
		List<File> files = null;
		try {
			files = fileManager.listEverything();
		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			log.error(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(files);
	}

	@GetMapping({"/list","/list/{parentId}"})
	public ResponseEntity<List<File>> list(@PathVariable(required = false) String parentId) throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listFolderContent(parentId);
		return ResponseEntity.ok(files);
	}

	@GetMapping("/download/{id}")
	public void download(@PathVariable String id, HttpServletResponse response) throws IOException, GeneralSecurityException {
		fileManager.downloadFile(id, response.getOutputStream());
	}

	@GetMapping("/directory/create")
	public ResponseEntity<String> createDirecory(@RequestParam String path) throws Exception {
		String parentId = fileManager.getFolderId(path);
		return ResponseEntity.ok("parentId: "+parentId);
	}

	@PostMapping(value = "/upload",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE} )
	public ResponseEntity<String> uploadSingleFileExample4(@RequestBody MultipartFile[] files,@RequestParam(required = false) String caseId,@RequestParam(required = false) String group,@RequestParam(required = false) String name) {
		String caseSpecficLoc;
		try {

			caseSpecficLoc = (Strings.isNullOrEmpty(name)) ? caseId + "/" + group
					: caseId + "/" + group + "/" + name;
			
			List<String> fileNames = new ArrayList<>();

			Arrays.asList(files).stream().forEach(file -> {
				log.info("Request contains, File: " + file.getOriginalFilename());
				String fileId = fileManager.uploadFile(file, caseId);
			
				fileNames.add(fileId);
			});
		//log.info("Request contains, File: " + file.getOriginalFilename());
		//String fileId = fileManager.uploadFile(file, caseId);
		if(fileNames.isEmpty() ){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok("Success, FileId: "+ fileNames);
	} catch (Exception e){
		log.error("Error while file upload"+e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
		}


	@GetMapping("/delete/{id}")
	public void delete(@PathVariable String id) throws Exception {
		fileManager.deleteFile(id);
	}
}
