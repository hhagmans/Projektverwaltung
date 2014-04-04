package controllers;

import java.util.List;

import models.Employee;
import models.Project_Employee;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;

public class EmployeeController extends Controller {

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
	 * Returned die Employeeview mit allen Daten des Employees
	 * 
	 * @param id
	 *            id des Employees
	 * @return
	 */
	public static Result employeeView(String id) {
		Employee emp = null;
		emp = Ebean.find(Employee.class, id);
		List<Project_Employee> associations = emp.getProjectAssociations();
		return ok(views.html.employeeView.render(emp, associations));
	}

}
