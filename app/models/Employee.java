package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

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

	public Employee(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public List<Project_Employee> getProjectAssociations() {
		return Ebean.find(Project_Employee.class).where()
				.eq("employee_id", this.id).findList();
	}

	public List<Project> getEmployees() {
		List<Project_Employee> list = getProjectAssociations();
		Iterator<Project_Employee> iter = list.iterator();
		ArrayList<Project> projList = new ArrayList<Project>();
		while (iter.hasNext()) {
			Project_Employee actAsso = iter.next();
			projList.add(Ebean.find(Project.class, actAsso.project_id));
		}
		return projList;
	}
}
