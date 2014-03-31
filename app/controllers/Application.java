package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.Employee;
import models.Project;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	/**
	 * Erstellt eine Liste von Employees aus der LiQID Applikation
	 * 
	 * @return Liste von Employees
	 */
	public static List<Employee> getAllEmployees(final String loginName,
			final String password) {
		// Authentifizierung am innoQ Server
		Authenticator.setDefault(new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(loginName, password
						.toCharArray());
			}
		});
		URL url = null;
		try {
			url = new URL(
					"https://intern.innoq.com/liqid/groups/Mitarbeiter.json");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		InputStream is = null;
		List<Employee> list = new ArrayList<Employee>();
		try {
			is = url.openStream();
			JsonNode json = Json.parse(is);
			Iterator<JsonNode> iter = json.get("member").iterator();

			while (iter.hasNext()) {
				JsonNode currentNode = iter.next();
				String id = currentNode.get("uid").textValue();
				String name = currentNode.get("displayName").textValue();
				if (!id.equals("admin"))
					list.add(new Employee(id, name));
			}
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gibt einen User aus der LiQID Applikation zurück.
	 * 
	 * @param id
	 * @return Employee mit id
	 */
	public static Employee getEmployee(String id) {

		URL url = null;
		try {
			url = new URL("https://intern.innoq.com/liqid/users/" + id
					+ ".json");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		InputStream is = null;
		try {
			is = url.openStream();
			JsonNode json = Json.parse(is);
			String name = json.get("displayName").textValue();
			return new Employee(id, name);
		} catch (IOException e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Erstellt ein Projekt mit den gegebenen Parametern
	 * 
	 * @param name
	 * @param description
	 * @param wikilink
	 * @param active
	 * @param startDate
	 * @param endDate
	 * @return true wenn Erstellung erfolgreich, sonst false
	 */
	public static boolean createProject(String name, String description,
			URL wikilink, boolean active, Date startDate, Date endDate) {
		try {
			Project project = new Project(name, description, wikilink, active,
					startDate, endDate);
			project.save();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Synchronisiert die Employee- Datenbank mit der Datenbank der LiQID
	 * Applikation
	 * 
	 * @param name
	 * @param password
	 * @return true wenn erfolgreich, sonst false
	 */
	public static boolean synchronizeDB(String name, String password) {
		List<Employee> allEmps = getAllEmployees(name, password);
		if (!allEmps.isEmpty()) {
			for (Employee employee : allEmps) {
				if (Ebean.find(Employee.class, employee.id) == null)
					employee.save();
				else if (Ebean.find(Employee.class, employee.id).name != employee.name)
					employee.update();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returned die Projektübersicht mit allen aktuellen Projekten
	 * 
	 * @return Projektübersicht
	 */
	public static Result projectOverview() {
		List<Project> allProjects = null;
		allProjects = Ebean.find(Project.class).findList();
		return ok(views.html.projectOverview.render(allProjects));
	}

	/**
	 * Returned die Employeeübersicht mit allen Emplyoees
	 * 
	 * @return Employeeübersicht
	 */
	public static Result employeeOverview() {
		List<Employee> allEmps = null;
		allEmps = Ebean.find(Employee.class).findList();
		return ok(views.html.employeeOverview.render(allEmps));
	}

	/**
	 * Returned die View des Projekts mit der gegebenen ID.
	 * 
	 * @param id
	 *            id des Projekts
	 * @return
	 */
	public static Result projectView(int id) {
		Project project = Ebean.find(Project.class, id);
		return ok(views.html.projectView.render(project));
	}

	/**
	 * Returned die View, auf der Projektdaten zum Erstellen eines Projektes
	 * angegeben werden können.
	 * 
	 * @return Projekterstellview
	 */
	public static Result projectAdd() {
		return ok(views.html.projectAdd.render());
	}

	/**
	 * Validiert die Form im Request, erstellt ggf. ein Projekt und returned die
	 * Adminview
	 * 
	 * @return Adminview
	 */
	public static Result projectAddSave() {
		DynamicForm bindedForm = form().bindFromRequest();
		String name = bindedForm.get("name");
		String description = bindedForm.get("description");
		URL wikilink;
		try {
			wikilink = new URL(bindedForm.get("wiki"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return badRequest(views.html.adminIndex
					.render("URL fehlerhaft, Projekt nicht erstellt."));
		}
		boolean active;
		if (bindedForm.get("active") == null)
			active = false;
		else
			active = true;

		Date startDate;
		Date endDate;
		try {
			startDate = new SimpleDateFormat("MM/dd/yyyy").parse(bindedForm
					.get("startdate"));

			endDate = new SimpleDateFormat("MM/dd/yyyy").parse(bindedForm
					.get("enddate"));
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(views.html.adminIndex
					.render("Datum fehlerhaft, Projekt nicht erstellt."));
		}
		boolean success = createProject(name, description, wikilink, active,
				startDate, endDate);
		if (success)
			return ok(views.html.adminIndex.render("Projekt erstellt!"));
		else
			return badRequest(views.html.adminIndex
					.render("Daten fehlerhaft, Projekt nicht erstellt."));

	}

	public static Result deleteProject(long id) {
		Project project = Ebean.find(Project.class, id);
		project.delete();
		List<Project> allProjects = null;
		allProjects = Ebean.find(Project.class).findList();
		return ok(views.html.projectOverview.render(allProjects));
	}

	/**
	 * Returned die View zum Projectedit
	 * 
	 * @return Projectedit
	 */
	public static Result projectEdit() {
		return ok(views.html.projectEdit.render());
	}

	/**
	 * Returned die View um einem Projekt einen Employee zuzufügen
	 * 
	 * @return
	 */
	public static Result projectEditAddEmployee() {
		return ok(views.html.projectEditAddUser.render());
	}

	/**
	 * Returned die Employeeview mit allen Daten des Employees
	 * 
	 * @param id
	 *            id des Employees
	 * @return
	 */
	public static Result employeeView(String id) {
		Employee emp = null;
		emp = Ebean.find(Employee.class, id);
		return ok(views.html.employeeView.render(emp));
	}

	/**
	 * Returned die View des Adminindex
	 * 
	 * @return Adminview
	 */
	public static Result adminIndex() {
		return ok(views.html.adminIndex.render(""));
	}

	/**
	 * Synced die Employeedatenbank mit der Datenbank der LiQID Applikation und
	 * returned dann die Adminview
	 * 
	 * @return Adminview
	 */
	public static Result adminSyncDb() {
		DynamicForm bindedForm = form().bindFromRequest();
		String name = bindedForm.get("name");
		String password = bindedForm.get("password");
		boolean success = synchronizeDB(name, password);
		String message;
		if (success)
			message = "Datenbank synchronisiert!";
		else
			message = "Logindaten falsch!";
		return ok(views.html.adminIndex.render(message));
	}
}
