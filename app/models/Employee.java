package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

/**
 * Beschreibt einen Mitarbeiter der Firma
 * 
 * @author hendrikh
 * 
 */
@Entity
public class Employee extends Model {

	private static final long serialVersionUID = 1L;

	// id entspricht dem Kürzel bspw. hendrikh
	@Id
	public String id;
	public String name;

	@OneToMany(mappedBy = "principalConsultant")
	public List<Project> principalConsultant;

	@OneToMany(mappedBy = "employee")
	public List<Project_Employee> projects;

	public Employee(String id, String name) {
		this.id = id;
		this.name = name;
	}

}
