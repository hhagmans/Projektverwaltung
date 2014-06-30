package models;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ProjectAssociationId implements Serializable {

	public String employeeId;

	public int projectId;

	public int hashCode() {
		return (int) (employeeId.hashCode() + projectId);
	}

	public boolean equals(Object object) {
		if (object instanceof ProjectAssociationId) {
			ProjectAssociationId otherId = (ProjectAssociationId) object;
			return (otherId.employeeId == this.employeeId)
					&& (otherId.projectId == this.projectId);
		}
		return false;
	}
}