package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Employee extends Model {

	private static final long serialVersionUID = 1L;

	// uid entspricht dem Kürzel bspw. hendrikh
	@Id
	public String uid;
	public String name;

	public Employee(String uid, String name) {
		this.uid = uid;
		this.name = name;
	}

}
