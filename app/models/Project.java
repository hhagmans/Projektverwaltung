package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import play.db.jpa.JPA;

/**
 * Beschreibt ein Projekt der Firma
 * 
 * @author hendrikh
 * 
 */
@Entity
public class Project implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;

	public String name;

	@Lob
	public String description;

	@Column(name = "wiki_link")
	public URL wikiLink;

	public boolean active;

	@Column(name = "start_date")
	public Date startDate;

	@Column(name = "end_date")
	public Date endDate;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.ALL)
	public List<ProjectEmployee> projectEmployee = new ArrayList<ProjectEmployee>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "principalConsultant")
	public Employee principalConsultant;

	public Project() {
	}

	public Project(String name, String description, URL wikiLink,
			boolean active, Date startDate, Date endDate) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	@Transient
	public List<Employee> getEmps() {
		List<Employee> emps = new ArrayList<>();
		for (ProjectEmployee pe : this.projectEmployee) {
			emps.add(pe.employee);
		}
		return emps;
	}

	/**
	 * Füge einen Employee zum Projekt hinzu inkl startDate und endDate
	 */
	public void addEmployee(Employee emp, Date startDate, Date endDate) {
		ProjectEmployee projEmp = new ProjectEmployee(this, emp, startDate,
				endDate);
		projectEmployee.add(projEmp);
		JPA.em().persist(this);
	}
}
