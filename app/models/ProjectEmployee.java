package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Mapping Klasse für die n:m Relation zwischen @Project und @Employee. Besitzt
 * die 2 Zusatzattribute startDate und endDate zum Ermitteln der
 * Zugehörigkeitszeit einzelner Mitarbeiter.
 * 
 * @author hendrikh
 * 
 */
@Entity
@Table(name = "project_employee")
public class ProjectEmployee implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "project_employee_id")
	public int id;

	public String employeeId;
	public int projectId;

	@ManyToOne
	@JoinColumn(name = "employeeId", updatable = false, insertable = false, referencedColumnName = "id")
	public Employee employee;

	@ManyToOne
	@JoinColumn(name = "projectId", updatable = false, insertable = false, referencedColumnName = "id")
	public Project project;

	@Temporal(TemporalType.DATE)
	@Column(name = "start_date")
	public Date startDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "end_date")
	public Date endDate;

	public ProjectEmployee() {
	}

	public ProjectEmployee(Project project, Employee employee, Date startDate,
			Date endDate) {
		this.employee = employee;
		this.project = project;
		this.startDate = startDate;
		this.endDate = endDate;
		this.employeeId = employee.id;
		this.projectId = project.id;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ProjectEmployee that = (ProjectEmployee) o;

		if (this.project != null ? !this.project.equals(that.project)
				: that.project != null)
			if (this.employee != null ? !this.employee.equals(that.employee)
					: that.employee != null)
				return false;

		return true;
	}

	public int hashCode() {
		return (this.project != null ? this.project.hashCode() : 0);
	}
}
