package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
@IdClass(ProjectAssociationId.class)
public class Project_Employee extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public int project_Employee_Id;

	public String employee_id;
	public int project_id;

	public Date startDate;
	public Date endDate;

	@ManyToOne
	@JoinColumn(name = "employee", updatable = false, insertable = false, referencedColumnName = "id")
	public Employee employee;

	@ManyToOne
	@JoinColumn(name = "project", updatable = false, insertable = false, referencedColumnName = "id")
	public Project project;

	public void setEmployee(Employee emp) {
		this.employee = emp;
		this.employee_id = emp.id;
	}

	public void setProject(Project project) {
		this.project = project;
		this.project_id = project.id;
	}

}