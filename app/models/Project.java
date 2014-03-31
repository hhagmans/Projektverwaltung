package models;

import java.net.URL;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

/**
 * Beschreibt ein Projekt der Firma
 * 
 * @author hendrikh
 * 
 */
@Entity
public class Project extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public int id;
	public String name;
	public String description;
	public URL wikiLink;
	public boolean active;
	public Date startDate;
	public Date endDate;

	public Project(String name, String description, URL wikiLink,
			boolean active, Date startDate, Date endDate) {
		this.name = name;
		this.description = description;
		this.wikiLink = wikiLink;
		this.active = active;
		this.startDate = startDate;
		this.endDate = endDate;
	}

}
