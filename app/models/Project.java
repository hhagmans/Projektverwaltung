package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

/**
 * Beschreibt ein Projekt der Firma
 * 
 * @author hendrikh
 * 
 */
@Entity
public class Project extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public int id;
	public String name;
	public String description;
	public URL wikiLink;
	public boolean active;
	public Date startDate;
	public Date endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "principalConsultant")
	public Employee principalConsultant;

	@OneToMany(mappedBy = "project")
	public List<Project_Employee> employees;

	public Project(String name, String description, URL wikiLink,
			boolean active, Date startDate, Date endDate) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
		this.principalConsultant = null;
		this.employees = new ArrayList<Project_Employee>();
	}

	public Project(String name, String description, URL wikiLink,
			boolean active, Date startDate, Date endDate,
			Employee principalConsultant) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
		this.principalConsultant = principalConsultant;
		this.employees = new ArrayList<Project_Employee>();
	}

	/**
	 * Füge einen Employee zum Projekt hinzu inkl startDate und endDate
	 * 
	 * @param employee
	 * @param teamLead
	 */
	public void addEmployee(Employee emp, Date startDate, Date endDate) {
		Project_Employee association = new Project_Employee();
		association.setEmployee(emp);
		association.setProject(this);
		association.startDate = startDate;
		association.endDate = endDate;

		this.employees.add(association);
		this.save();

		// Association Objekt auch im Employee verlinken
		emp.projects.add(association);
		emp.save();

		association.save();
	}

	public List<Project_Employee> getEmployeeAssociations() {
		return Ebean.find(Project_Employee.class).where()
				.eq("project_id", this.id).findList();
	}

	public List<Employee> getEmployees() {
		List<Project_Employee> list = getEmployeeAssociations();
		Iterator<Project_Employee> iter = list.iterator();
		ArrayList<Employee> empList = new ArrayList<Employee>();
		while (iter.hasNext()) {
			Project_Employee actAsso = iter.next();
			empList.add(Ebean.find(Employee.class, actAsso.employee_id));
		}
		return empList;
	}
}
