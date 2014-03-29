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

	public static List<Employee> getAllEmployees(final String loginName,
			final String password) {
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
				String uid = currentNode.get("uid").textValue();
				String name = currentNode.get("displayName").textValue();
				if (!uid.equals("admin"))
					list.add(new Employee(uid, name));
			}
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Employee getEmployee(String uid) {

		URL url = null;
		try {
			url = new URL("https://intern.innoq.com/liqid/users/" + uid
					+ ".json");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		InputStream is = null;
		try {
			is = url.openStream();
			JsonNode json = Json.parse(is);
			String name = json.get("displayName").textValue();
			return new Employee(uid, name);
		} catch (IOException e) {
			e.printStackTrace();

			return null;
		}
	}

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

	public static boolean synchronizeDB(String name, String password) {
		List<Employee> allEmps = getAllEmployees(name, password);
		if (!allEmps.isEmpty()) {
			for (Employee employee : allEmps) {
				if (Ebean.find(Employee.class, employee.uid) == null)
					employee.save();
				else if (Ebean.find(Employee.class, employee.uid).name != employee.name)
					employee.update();
			}
			return true;
		} else {
			return false;
		}
	}

	public static Result projectOverview() {
		List<Project> allProjects = null;
		allProjects = Ebean.find(Project.class).findList();
		return ok(views.html.projectOverview.render(allProjects));
	}

	public static Result employeeOverview() {
		List<Employee> allEmps = null;
		allEmps = Ebean.find(Employee.class).findList();
		return ok(views.html.employeeOverview.render(allEmps));
	}

	public static Result projectView(int id) {
		Project project = Ebean.find(Project.class, id);
		return ok(views.html.projectView.render());
	}

	public static Result projectAdd() {
		return ok(views.html.projectAdd.render());
	}

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

	public static Result projectEdit() {
		return ok(views.html.projectEdit.render());
	}

	public static Result projectEditAddEmployee() {
		return ok(views.html.projectEditAddUser.render());
	}

	public static Result employeeView(String id) {
		Employee emp = null;
		emp = Ebean.find(Employee.class, id);
		return ok(views.html.employeeView.render(emp));
	}

	public static Result adminIndex() {
		return ok(views.html.adminIndex.render(""));
	}

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
