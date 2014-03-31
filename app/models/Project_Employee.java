package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@IdClass(ProjectAssociationId.class)
public class Project_Employee {
	public Date startDate;
	public Date endDate;

	@ManyToOne
	@JoinColumn(name = "employeeId", updatable = false, insertable = false, referencedColumnName = "id")
	public Employee employee;

	@ManyToOne
	@JoinColumn(name = "projectId", updatable = false, insertable = false, referencedColumnName = "id")
	public Project project;

}
