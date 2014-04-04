package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import models.Employee;
import models.Project;
import models.Project_Employee;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	public static String byte2HexStr(byte binary) {
		StringBuffer sb = new StringBuffer();
		int hex;

		hex = (int) binary & 0x000000ff;
		if (0 != (hex & 0xfffffff0)) {
			sb.append(Integer.toHexString(hex));
		} else {
			sb.append("0" + Integer.toHexString(hex));
		}
		return sb.toString();
	}

	public static String encryptPassword(String password) {
		DESKeySpec dk;
		SecretKey secretKey = null;
		try {
			dk = new DESKeySpec(new Long(7490854493772951678L).toString()
					.getBytes());
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			secretKey = kf.generateSecret(dk);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Cipher c;

		try {
			c = Cipher.getInstance("DES/ECB/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encrypted;
			encrypted = c.doFinal(password.getBytes());

			// convert into hexadecimal number, and return as character string.
			String result = "";
			for (int i = 0; i < encrypted.length; i++) {
				result += byte2HexStr(encrypted[i]);
			}

			return result;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String decryptPassword(String password) {
		DESKeySpec dk;
		SecretKey secretKey = null;
		try {
			dk = new DESKeySpec(new Long(7490854493772951678L).toString()
					.getBytes());
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			secretKey = kf.generateSecret(dk);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Cipher c;

		try {
			byte[] tmp = new byte[password.length() / 2];
			int index = 0;
			while (index < password.length()) {
				// convert hexadecimal number into decimal number.
				int num = Integer.parseInt(
						password.substring(index, index + 2), 16);

				// convert into signed byte.
				if (num < 128) {
					tmp[index / 2] = new Byte(Integer.toString(num))
							.byteValue();
				} else {
					tmp[index / 2] = new Byte(
							Integer.toString(((num ^ 255) + 1) * -1))
							.byteValue();
				}
				index += 2;
			}

			c = Cipher.getInstance("DES/ECB/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(c.doFinal(tmp));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return "";
	}

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
				return new PasswordAuthentication(loginName, decryptPassword(
						password).toCharArray());
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
	 * Fügt den gegebenen Employee dem Projekt zu.
	 * 
	 * @param emp
	 *            Employee Objekt
	 * @param startDate
	 *            Beginn des Aufenthalts im Projekt
	 * @param endDate
	 *            Ende des Aufenthalts im Projekt
	 * @return true wenn Erstellung erfolgreich, sonst false
	 */
	public static boolean addEmployeeToProject(Employee emp, Project project,
			Date startDate, Date endDate) {
		try {
			project.addEmployee(emp, startDate, endDate);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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

	/**
	 * Löscht das Projekt mit der übergebenen id.
	 * 
	 * @param id
	 * @return Projektübersicht aller Projekte
	 */
	public static Result deleteProject(long id) {
		Project project = Ebean.find(Project.class, id);
		project.delete();
		List<Project> allProjects = null;
		allProjects = Ebean.find(Project.class).findList();
		return ok(views.html.projectOverview.render(allProjects));
	}

	/**
	 * Gibt alle Employees für das Projekt der übergebenen id zurück
	 * 
	 * @param projectid
	 * @return
	 */
	public static List<Employee> getEmployeesforProject(long projectid) {
		Project project = Ebean.find(Project.class, projectid);
		List<Project_Employee> projemps = Ebean.find(Project_Employee.class)
				.findList();
		List<Employee> emps = new ArrayList();
		Iterator<Project_Employee> iter = projemps.iterator();
		while (iter.hasNext()) {
			Project_Employee projemp = iter.next();
			if (projemp.project.equals(project))
				emps.add(projemp.employee);
		}
		return emps;
	}

	/**
	 * Parsed die gegebene Formvalue und gibt den entsprechenden Employee
	 * zurück.
	 * 
	 * @param formValue
	 * @return
	 */
	public static Employee getEmployeeFromForm(String formValue) {
		String[] splittedValue = formValue.split("\\(");
		splittedValue = splittedValue[1].split("\\)");
		return Ebean.find(Employee.class, splittedValue[0]);
	}

	/**
	 * Returned die View zum Projectedit
	 * 
	 * @return Projectedit
	 */
	public static Result projectEdit(long id) {
		Project project = Ebean.find(Project.class, id);
		return ok(views.html.projectEdit.render(project, ""));
	}

	public static Result projectEditSave(long id) {
		Project project = Ebean.find(Project.class, id);
		DynamicForm bindedForm = form().bindFromRequest();
		project.name = bindedForm.get("name");
		project.description = bindedForm.get("description");
		URL wikilink;
		try {
			wikilink = new URL(bindedForm.get("wiki"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return badRequest(views.html.projectEdit.render(project,
					"URL fehlerhaft, Projekt nicht geupdated."));
		}
		project.wikiLink = wikilink;

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
			return badRequest(views.html.projectEdit.render(project,
					"Datum fehlerhaft, Projekt nicht geupdated."));
		}
		Employee principal = getEmployeeFromForm(bindedForm.get("principal"));

		return ok(views.html.projectView.render(project));
	}

	/**
	 * Returned die View um einem Projekt einen Employee zuzufügen
	 * 
	 * @return
	 */
	public static Result projectEditAddEmployee(long id) {
		Project project = Ebean.find(Project.class, id);
		List<Employee> allEmps = Ebean.find(Employee.class).findList();
		return ok(views.html.projectEditAddUser.render(project, allEmps, ""));
	}

	public static Result projectEditAddEmployeeSave(long id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = Ebean.find(Project.class, id);
		Date startDate;
		Date endDate;
		try {
			startDate = new SimpleDateFormat("MM/dd/yyyy").parse(bindedForm
					.get("startdate"));

			endDate = new SimpleDateFormat("MM/dd/yyyy").parse(bindedForm
					.get("enddate"));
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(views.html.projectEditAddUser.render(project,
					Ebean.find(Employee.class).findList(),
					"Datum fehlerhaft, User nicht hinzugefügt."));
		}

		Employee emp = getEmployeeFromForm(bindedForm.get("newEmp"));

		boolean ok = addEmployeeToProject(emp, project, startDate, endDate);

		if (ok)
			return ok(views.html.projectEdit.render(project, ""));
		else
			return ok(views.html.projectEdit.render(project,
					"Beim Hinzufügen des Users ist ein  Problem aufgetreten."));
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
		boolean success = synchronizeDB(name, encryptPassword(password));
		String message;
		if (success)
			message = "Datenbank synchronisiert!";
		else
			message = "Logindaten falsch!";
		return ok(views.html.adminIndex.render(message));
	}
}
