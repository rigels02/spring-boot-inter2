package org.rb.model;

import org.rb.entity.Person;
import org.springframework.web.multipart.MultipartFile;

public class PersonForm extends Person{

	private static final long serialVersionUID = 1L;
	 private MultipartFile[] fileDatas;

	public PersonForm() {
		super();
		
	}

	public PersonForm(String name, int age) {
		super(name, age);
		
	}
	
	public PersonForm(Person person) {
		super(person.getName(), person.getAge());
		setId(person.getId());
	}

	public void updatePerson(Person other) {
		other.setName(this.getName());
		other.setAge(this.getAge());
		other.setImage(this.getImage());
		
	}
	public MultipartFile[] getFileDatas() {
		return fileDatas;
	}

	public void setFileDatas(MultipartFile[] fileDatas) {
		this.fileDatas = fileDatas;
	}

	
	
}
