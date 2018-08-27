package org.rb.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.rb.entity.Person;
import org.rb.model.PersonForm;
import org.rb.repo.PersonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class PersonController {

	private static final String VIEW_PERSONS_DELETE_ID = "/view/persons/delete/{id}";
	private static final String VIEW_PERSONS_CREATE = "/view/persons/create";
	private static final String VIEW_EDIT_PERSONS = "/view/edit/persons";
	private static final String VIEW_PERSONS_ID = "/view/persons/{id}";
	private static final String PERSONS_VIEW = "/view/persons";
	private static final String PERSONS = "/persons";

	@Autowired
	PersonRepo repo;
	private String uploadRootPath;

	@GetMapping(PERSONS)
	public ResponseEntity<Iterable<Person>> getPersons() {
		Iterable<Person> persons = repo.findAll();

		return ResponseEntity.ok(persons);
	}

	@GetMapping(PERSONS_VIEW)
	public String getPersonsView(Model model) {
		Iterable<Person> persons = repo.findAll();
		model.addAttribute("persons", persons);

		return "persons";
	}

	@GetMapping(VIEW_PERSONS_ID)
	public String getPersonsById(@PathVariable("id") long id, Model model) {
		Optional<Person> operson = repo.findById(id);
		Person person = operson.get();
		model.addAttribute("person", new PersonForm(person));
		//model.addAttribute("UploadForm", new UploadForm());
		return "person";
	}
	@PostMapping(path = VIEW_EDIT_PERSONS)
    public String postPerson(
    		HttpServletRequest request, 
    		@Valid PersonForm personFrm, 
    		BindingResult result, 
    		Model model) 
	{
		
        if (result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            StringBuilder sb = new StringBuilder();
            for (int i=0; i< errors.size(); i++) {
                FieldError el= errors.get(i);
                sb.append(el.getField()).append(":").append(el.getDefaultMessage());
                if (i< errors.size()-1)
                    sb.append(", ");
            }
            model.addAttribute("errors", sb.toString());
            model.addAttribute("person", personFrm);
            return "person.html";
        }
        putPersonImage(request, personFrm);
        //is existing or new
        Person person;
        if(personFrm.getId()>0) {
        person = repo.findById(personFrm.getId()).get();
        } else {
        	person = new Person();
        }
        personFrm.updatePerson(person);
        repo.save(person);
        
        Iterable<Person> persons = repo.findAll();
        model.addAttribute("persons", persons);
        return "persons.html";
    }

	
	@GetMapping(VIEW_PERSONS_CREATE)
    public String getViewPersonCreate(Model model) {
        
        model.addAttribute("person", new PersonForm("", 0));
        //model.addAttribute("UploadForm", new UploadForm());
        return "person";
    }
	
	@GetMapping(VIEW_PERSONS_DELETE_ID)
    public String deletePerson(@PathVariable("id") long id, Model model) {
        Optional<Person> ofound = repo.findById(id);
        if (ofound.isPresent())
            repo.delete(ofound.get());
        Iterable<Person> persons = repo.findAll();
        model.addAttribute("persons", persons);
        return "persons.html";
    }
	
	/**
	 * Download image by Id
	 * @param id
	 * @return
	 */
	@GetMapping("/view/persons/image/{id}")
	public ResponseEntity<ByteArrayResource> downloadImage(@PathVariable("id") long id){
		
		Optional<Person> person = repo.findById(id);
		if( !person.isPresent()) {
			return ResponseEntity.notFound().build();
			
		}
		byte[] image = person.get().getImage();
		ByteArrayResource resource = new ByteArrayResource(image);
		
		return ResponseEntity.ok()
				// Content-Disposition
                //.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "image.png")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + "image.png")
                // Content-Type
                .contentType(MediaType.APPLICATION_OCTET_STREAM) //
                // Content-Lengh
                .contentLength(image.length) //
                .body(resource);
	}
	
	/**
	 * View image in Web browser
	 * @param id
	 * @param response
	 */
	@GetMapping("/view/persons/imageV/{id}")
	public void getImage(
			@PathVariable("id") long id,
			HttpServletResponse response
			){
		
		Optional<Person> person = repo.findById(id);
		if( !person.isPresent()) {
			System.err.println("Not found...");
			return;
			
		}
		byte[] image = person.get().getImage();
		
		response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
		try {
			ServletOutputStream os = response.getOutputStream();
			os.write(image);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Redirect to imagecard.html from where image is rendered 
	 * (by using image src)
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/view/persons/imageCard/{id}")
	public String getImageCard(
			@PathVariable("id") long id,
			Model model
			){
		
		model.addAttribute("id", id);
		
		return "imagecard";
	}
	
	//----------- Image tools ---------------//
	
	private synchronized void putPersonImage(HttpServletRequest request, PersonForm personFrm) {
		doFileUpload(request,personFrm);
        personFrm.setImage(getUploadedImage());
	}
	
	private void doFileUpload(HttpServletRequest request, PersonForm personFrm) {
		 // Root Directory.
	      uploadRootPath = request.getServletContext().getRealPath("upload");
	      System.out.println("uploadRootPath=" + uploadRootPath);
	 
	      File uploadRootDir = new File(uploadRootPath);
	      // Create directory if it not exists.
	      if (!uploadRootDir.exists()) {
	         uploadRootDir.mkdirs();
	      }
	      MultipartFile[] fileDatas = personFrm.getFileDatas();
	      int fileIdx=0;
	      for (MultipartFile fileData : fileDatas) {
			File serverFile = new File(uploadRootDir.getAbsolutePath()+File.separator+"image"+fileIdx+".png");
			fileIdx++;
			//stream out
			
			try {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
			stream.write(fileData.getBytes());
            stream.close();
			}catch (Exception e) {
				
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Only one file is used at the moment
	 * @return
	 */
	private byte[] getUploadedImage() {
		if (uploadRootPath == null)
			return null;
		File imgDir = new File(uploadRootPath);
		if (!imgDir.exists())
			return null;

		Path path = Paths.get(imgDir.getAbsolutePath() + File.separator + "image0.png");
		if (!Files.exists(path))
			return null;
		
		try {
			byte[] imageBytes = Files.readAllBytes(path);
			path.toFile().delete();
			if(imageBytes.length==0) return null;
			return imageBytes;
		} catch (IOException e) {

			e.printStackTrace();
			System.err.println(e.getMessage());
		}

		return null;
	}

}
