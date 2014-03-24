package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Employee;
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
		return ok(views.html.projectOverview.render());
	}

	public static Result employeeOverview() {
		List<Employee> allEmps = null;
		allEmps = Ebean.find(Employee.class).findList();
		return ok(views.html.employeeOverview.render(allEmps));
	}

	public static Result projectView() {
		return ok(views.html.projectView.render());
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
