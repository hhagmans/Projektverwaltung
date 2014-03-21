package models;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Project extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public String id;
	public String name;
	public String description;
	public URL wikiLink;
	public boolean active;

	public Project(String name, String description, URL wikiLink, boolean active) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
	}

}
