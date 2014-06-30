package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Beschreibt einen Mitarbeiter der Firma
 * 
 * @author hendrikh
 * 
 */
@Entity
public class Employee implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	// id entspricht dem Kürzel bspw. hendrikh
	@Id
	public String id;

	public String name;

	public boolean isPrincipal;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "employee", cascade = CascadeType.ALL)
	public List<ProjectEmployee> projectEmployee;

	public Employee() {
	}

	public Employee(String id, String name, boolean isPrincipal) {
		this.id = id;
		this.name = name;
		this.isPrincipal = isPrincipal;
	}

	@Transient
	public List<Project> getProjectAssociations() {
		List<Project> projects = new ArrayList<Project>();
		for (ProjectEmployee pe : this.projectEmployee) {
			projects.add(pe.project);
		}
		return projects;
	}

	@Transient
	public List<Project> getEmployees() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
