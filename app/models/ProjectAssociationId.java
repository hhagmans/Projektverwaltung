package models;

import java.io.Serializable;

public class ProjectAssociationId implements Serializable {

	private long employeeId;

	private long projectId;

	public int hashCode() {
		return (int) (employeeId + projectId);
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