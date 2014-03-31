package models;

import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

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
	public long id;
	public String name;
	public String description;
	public URL wikiLink;
	public boolean active;
	public Date startDate;
	public Date endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "principalConsultant")
	private Employee principalConsultant;

	@OneToMany(mappedBy = "project")
	private List<Project_Employee> employees;

	public Project(String name, String description, URL wikiLink,
			boolean active, Date startDate, Date endDate) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
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
	}

	/**
	 * Füge einen Employee zum Projekt hinzu inkl startDate und endDate
	 * 
	 * @param employee
	 * @param teamLead
	 */
	public void addEmployee(Employee employee, Date startDate, Date endDate) {
		Project_Employee association = new Project_Employee();
		association.employee = employee;
		association.project = this;
		association.startDate = startDate;
		association.endDate = endDate;

		this.employees.add(association);
		// Also add the association object to the employee.
		employee.projects.add(association);
	}
}
