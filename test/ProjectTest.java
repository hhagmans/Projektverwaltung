import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Employee;
import models.Project;
import models.Project_Employee;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;

public class ProjectTest extends WithApplication {
	static FakeApplication app;

	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase()));
	}

	@AfterClass
	public static void stopApp() {
		Helpers.stop(app);
	}

	@Test
	public void testaddProject() throws MalformedURLException {

		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();
		assertEquals(1, Ebean.find(Project.class).findList().size());
		assertNotNull(Ebean.find(Project.class, 1));

	}

	@Test
	public void testdeleteProject() throws MalformedURLException {
		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();
		assertEquals(1, Ebean.find(Project.class).findList().size());
		assertNotNull(Ebean.find(Project.class, 1));

		Ebean.find(Project.class, 1).delete();
		assertEquals(0, Ebean.find(Project.class).findList().size());

	}

	@Test
	public void testupdateProject() throws MalformedURLException {
		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();
		assertEquals("testProject", Ebean.find(Project.class, 1).name);

		Project proj = Ebean.find(Project.class, 1);
		proj.name = "newTestProject";
		proj.save();

		assertEquals("newTestProject", Ebean.find(Project.class, 1).name);

	}

	@Test
	public void testaddEmployeeToProject() throws MalformedURLException {
		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();

		Employee emp = new Employee("emp", "Employee");
		Project proj = Ebean.find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		proj.save();

		assertEquals(1, proj.getEmployees().size());

	}

	@Test
	public void testdeleteEmployeeFromProject() throws MalformedURLException {
		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();

		Employee emp = new Employee("emp", "Employee");
		emp.save();
		Project proj = Ebean.find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		proj.save();

		assertEquals(1, proj.getEmployees().size());

		proj.getEmployeeAssociations().get(0).delete();

		assertEquals(0, proj.getEmployees().size());

	}

	@Test
	public void testupdateEmployeeInProject() throws MalformedURLException,
			ParseException {
		new Project("testProject", "TestDescription", new URL(
				"http://www.innoq.com"), false, new Date(), new Date()).save();

		Employee emp = new Employee("emp", "Employee");
		Project proj = Ebean.find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		proj.save();

		assertEquals(1, proj.getEmployees().size());

		Project_Employee association = proj.getEmployeeAssociations().get(0);

		association.startDate = new SimpleDateFormat("MM/dd/yyyy")
				.parse("11/11/2014");
		association.save();

		assertEquals(new SimpleDateFormat("MM/dd/yyyy").parse("11/11/2014"),
				proj.getEmployeeAssociations().get(0).startDate);

	}
}