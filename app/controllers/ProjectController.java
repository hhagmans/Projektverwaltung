package controllers;

import static play.data.Form.form;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.Employee;
import models.Project;
import models.Project_Employee;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;

public class ProjectController extends Controller {
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
	 * Validiert das übergebenen Formvalue und parsed ein @Date aus dem
	 * übergebenen Formvalue.
	 * 
	 * @param formValue
	 * @return
	 * @throws ParseException
	 */
	public static Date getDateFromForm(String formValue) throws ParseException {
		Date date;
		Logger.debug(formValue);
		if (formValue == "") {
			date = null;
		} else {
			if (!formValue.matches("\\d{2}/\\d{2}/\\d{4}")) {
				throw new ParseException(
						" Startdate does not match the correct regex.", 370);
			}
			date = new SimpleDateFormat("MM/dd/yyyy").parse(formValue);
		}
		return date;
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
	 * Returned die View des Projekts mit der gegebenen ID.
	 * 
	 * @param id
	 *            id des Projekts
	 * @return
	 */
	public static Result projectView(int id) {
		Project project = Ebean.find(Project.class, id);
		List<Project_Employee> associations = project.getEmployeeAssociations();
		return ok(views.html.projectView.render(project, associations));
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
			startDate = getDateFromForm(bindedForm.get("startdate"));

			endDate = getDateFromForm(bindedForm.get("enddate"));
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
		List<Project_Employee> associations = project.getEmployeeAssociations();
		for (Project_Employee project_Employee : associations) {
			project_Employee.delete();
		}
		project.delete();
		List<Project> allProjects = null;
		allProjects = Ebean.find(Project.class).findList();
		return ok(views.html.projectOverview.render(allProjects));
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

	public static Project_Employee getAssociation(Project project, Employee emp) {
		Project_Employee association = null;
		List<Project_Employee> associations = project.getEmployeeAssociations();
		Iterator<Project_Employee> iter = associations.iterator();
		while (iter.hasNext()) {
			Project_Employee currentAsso = iter.next();
			if (currentAsso.getEmployee().equals(emp)) {
				association = currentAsso;
			}
		}
		return association;
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

	/**
	 * Speichert die im POST übergebenen Attribute im Projekt mit der
	 * übergebenen id
	 * 
	 * @param id
	 * @return Die Projektsicht des Projekts mit der übergebenen id
	 */
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

		project.active = active;

		Date startDate;
		Date endDate;
		try {
			startDate = getDateFromForm(bindedForm.get("startdate"));

			endDate = getDateFromForm(bindedForm.get("enddate"));
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(views.html.projectEdit.render(project,
					"Datum fehlerhaft, Projekt nicht geupdated."));
		}

		project.startDate = startDate;
		project.endDate = endDate;

		if (bindedForm.get("principal") != "") {
			Employee principal = getEmployeeFromForm(bindedForm
					.get("principal"));

			project.principalConsultant = principal;
		}

		project.save();

		return ok(views.html.projectView.render(project,
				project.getEmployeeAssociations()));
	}

	/**
	 * Returned die View um einem Projekt einen Employee zuzufügen
	 * 
	 * @return
	 */
	public static Result projectEditAddEmployee(long id) {
		Project project = Ebean.find(Project.class, id);
		List<Employee> allEmps = Ebean.find(Employee.class).findList();
		allEmps.removeAll(project.getEmployees());
		return ok(views.html.projectEditAddUser.render(project, allEmps, ""));
	}

	public static Result projectEditAddEmployeeSave(long id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = Ebean.find(Project.class, id);
		Date startDate;
		Date endDate;
		try {
			startDate = getDateFromForm(bindedForm.get("startdate"));

			endDate = getDateFromForm(bindedForm.get("enddate"));
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
	 * Lösche Employee aus POST in Projectk mit übergebener id.
	 * 
	 * @param id
	 * @return
	 */
	public static Result projectEditDeleteEmployee(long id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = Ebean.find(Project.class, id);

		Employee emp = getEmployeeFromForm(bindedForm.get("selectedEmp"));

		List<Project_Employee> associations = project.getEmployeeAssociations();

		Iterator<Project_Employee> iter = associations.iterator();

		while (iter.hasNext()) {
			Project_Employee asso = iter.next();
			if (asso.getEmployee().equals(emp)) {
				asso.delete();
				break;
			}
		}

		if (emp.equals(project.principalConsultant)) {
			project.principalConsultant = null;
			project.save();
		}

		return ok(views.html.projectEdit.render(project,
				"Der Mitarbeiter wurde erfolgreich aus dem Projekt gelöscht!"));
	}

	/**
	 * Returned die View um einen Employee innerhalb eines Projektes
	 * (@Project_Employee) zu editieren.
	 * 
	 * @param id
	 * @return
	 */
	public static Result projectEditEditEmployee(long id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = Ebean.find(Project.class, id);
		if (bindedForm.get("selectedEmp") == "") {
			return badRequest(views.html.projectEdit.render(project,
					"Kein Mitarbeiter ausgewählt!"));
		}
		Employee emp = getEmployeeFromForm(bindedForm.get("selectedEmp"));
		Project_Employee association = getAssociation(project, emp);
		return ok(views.html.projectEditEditUser.render(project, association));
	}

	/**
	 * Ändert das @Project_Employee Objekt entsprechend der übergebenen
	 * Attribute ab.
	 * 
	 * @param id
	 * @return
	 */
	public static Result projectEditEditEmployeeSave(long id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = Ebean.find(Project.class, id);

		Date startDate;
		Date endDate;
		try {
			startDate = getDateFromForm(bindedForm.get("startdate"));

			endDate = getDateFromForm(bindedForm.get("enddate"));
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(views.html.projectEdit.render(project,
					"Datum fehlerhaft, Employee nicht geupdated."));
		}

		Project_Employee association = getAssociation(project,
				getEmployeeFromForm(bindedForm.get("emp")));

		association.startDate = startDate;
		association.endDate = endDate;
		association.save();

		return ok(views.html.projectEdit.render(project,
				"Employee erfolgreich geupdated!"));
	}
}
