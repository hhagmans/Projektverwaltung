package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

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

	public void setEmployee(Employee emp) {
		this.employee_id = emp.id;
	}

	public void setProject(Project project) {
		this.project_id = project.id;
	}

	public Employee getEmployee() {
		return Ebean.find(Employee.class, this.employee_id);
	}

	public Project getProject() {
		return Ebean.find(Project.class, this.project_id);
	}

}
