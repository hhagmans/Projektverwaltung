package controllers;

import java.util.List;

import models.Employee;
import models.ProjectEmployee;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class EmployeeController extends Controller {

	/**
	 * Returned die Employeeübersicht mit allen Emplyoees
	 * 
	 * @return Employeeübersicht
	 */
	@Transactional
	public static Result employeeOverview() {
		List<Employee> allEmps = JPA.em()
				.createQuery("Select e from Employee e", Employee.class)
				.getResultList();
		return ok(views.html.employeeOverview.render(allEmps));
	}

	/**
	 * Returned die Employeeview mit allen Daten des Employees
	 * 
	 * @param id
	 *            id des Employees
	 * @return
	 */
	@Transactional
	public static Result employeeView(String id) {
		Employee emp = null;
		emp = JPA.em().find(Employee.class, id);
		List<ProjectEmployee> associations = emp.projectEmployee;

		return ok(views.html.employeeView.render(emp, associations));
	}

}
