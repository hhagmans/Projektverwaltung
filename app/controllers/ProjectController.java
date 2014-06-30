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
import models.ProjectEmployee;
import play.Logger;
import play.data.DynamicForm;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

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
			JPA.em().persist(project);
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
	@Transactional
	public static Result projectOverview() {
		List<Project> allProjects = null;
		allProjects = JPA.em()
				.createQuery("Select p from Project p", Project.class)
				.getResultList();
		return ok(views.html.projectOverview.render(allProjects));
	}

	/**
	 * Returned die View des Projekts mit der gegebenen ID.
	 * 
	 * @param id
	 *            id des Projekts
	 * @return
	 */
	@Transactional
	public static Result projectView(int id) {
		Project project = JPA.em().find(Project.class, id);
		List<ProjectEmployee> associations = project.projectEmployee;

		Employee principal = project.principalConsultant;
		return ok(views.html.projectView.render(project, associations,
				principal));
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
	@Transactional
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
	@Transactional
	public static Result deleteProject(int id) {
		List<Project> allProjects = null;
		allProjects = JPA.em()
				.createQuery("Select p from Project p", Project.class)
				.getResultList();

		Project project = JPA.em().find(Project.class, id);
		List<ProjectEmployee> associations = project.projectEmployee;
		for (ProjectEmployee project_Employee : associations) {
			JPA.em().remove(project_Employee);
		}
		JPA.em().remove(project);

		allProjects.remove(project);

		return ok(views.html.projectOverview.render(allProjects));
	}

	/**
	 * Parsed die gegebene Formvalue und gibt den entsprechenden Employee
	 * zurück.
	 * 
	 * @param formValue
	 * @return
	 */
	@Transactional
	public static Employee getEmployeeFromForm(String formValue) {
		String[] splittedValue = formValue.split("\\(");
		splittedValue = splittedValue[1].split("\\)");
		return JPA.em().find(Employee.class, splittedValue[0]);
	}

	public static ProjectEmployee getAssociation(Project project, Employee emp) {
		ProjectEmployee association = null;
		List<ProjectEmployee> associations = project.projectEmployee;
		Iterator<ProjectEmployee> iter = associations.iterator();
		while (iter.hasNext()) {
			ProjectEmployee currentAsso = iter.next();
			if (currentAsso.employee.equals(emp)) {
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
	@Transactional
	public static Result projectEdit(long id) {
		Project project = JPA.em().find(Project.class, (int) id);
		return ok(views.html.projectEdit.render(project, ""));
	}

	/**
	 * Speichert die im POST übergebenen Attribute im Projekt mit der
	 * übergebenen id
	 * 
	 * @param id
	 * @return Die Projektsicht des Projekts mit der übergebenen id
	 */
	@Transactional
	public static Result projectEditSave(int id) {
		Project project = JPA.em().find(Project.class, id);
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

		JPA.em().persist(project);

		Employee principal = project.principalConsultant;

		List<ProjectEmployee> projectEmployees = project.projectEmployee;
		return ok(views.html.projectView.render(project, projectEmployees,
				principal));
	}

	/**
	 * Returned die View um einem Projekt einen Employee zuzufügen
	 * 
	 * @return
	 */
	@Transactional
	public static Result projectEditAddEmployee(int id) {
		Project project = JPA.em().find(Project.class, id);
		List<Employee> allEmps = JPA.em()
				.createQuery("Select e from Employee e", Employee.class)
				.getResultList();
		allEmps.removeAll(project.getEmps());

		return ok(views.html.projectEditAddUser.render(project, allEmps, ""));
	}

	@Transactional
	public static Result projectEditAddEmployeeSave(int id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = JPA.em().find(Project.class, id);
		Date startDate;
		Date endDate;
		try {
			startDate = getDateFromForm(bindedForm.get("startdate"));

			endDate = getDateFromForm(bindedForm.get("enddate"));
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(views.html.projectEditAddUser.render(project,
					null, // JPA.em().find(Employee.class).findList(),
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
	 * Lösche Employee aus POST in Projekt mit übergebener id.
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public static Result projectEditDeleteEmployee(int id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = JPA.em().find(Project.class, id);

		Employee emp = getEmployeeFromForm(bindedForm.get("selectedEmp"));

		List<ProjectEmployee> associations = project.projectEmployee;

		Iterator<ProjectEmployee> iter = associations.iterator();

		while (iter.hasNext()) {
			ProjectEmployee asso = iter.next();
			if (asso.employee.equals(emp)) {
				project.projectEmployee.remove(asso);
				JPA.em().persist(project);
				JPA.em().remove(asso);
				break;
			}
		}

		return ok(views.html.projectEdit.render(project,
				"Der Mitarbeiter wurde erfolgreich aus dem Projekt gelöscht!"));
	}

	/**
	 * Returned die View um einen Employee innerhalb eines Projektes
	 * (@ProjectEmployee) zu editieren.
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public static Result projectEditEditEmployee(int id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = JPA.em().find(Project.class, id);
		if (bindedForm.get("selectedEmp") == "") {
			return badRequest(views.html.projectEdit.render(project,
					"Kein Mitarbeiter ausgewählt!"));
		}
		Employee emp = getEmployeeFromForm(bindedForm.get("selectedEmp"));
		ProjectEmployee association = getAssociation(project, emp);
		return ok(views.html.projectEditEditUser.render(project, association));
	}

	/**
	 * Ändert das @ProjectEmployee Objekt entsprechend der übergebenen Attribute
	 * ab.
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public static Result projectEditEditEmployeeSave(int id) {
		DynamicForm bindedForm = form().bindFromRequest();
		Project project = JPA.em().find(Project.class, id);

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

		ProjectEmployee association = getAssociation(project,
				getEmployeeFromForm(bindedForm.get("emp")));

		association.startDate = startDate;
		association.endDate = endDate;
		JPA.em().persist(association);

		return ok(views.html.projectEdit.render(project,
				"Employee erfolgreich geupdated!"));
	}
}
