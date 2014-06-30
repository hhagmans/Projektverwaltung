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
import models.ProjectEmployee;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

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

		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));
		assertEquals(1,
				JPA.em().createQuery("Select p from Project p", Project.class)
						.getResultList().size());
		assertNotNull(JPA.em().find(Project.class, 1));

	}

	@Test
	public void testdeleteProject() throws MalformedURLException {
		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));
		assertEquals(1,
				JPA.em().createQuery("Select p from Project p", Project.class)
						.getResultList().size());
		assertNotNull(JPA.em().find(Project.class, 1));

		JPA.em().remove(JPA.em().find(Project.class, 1));
		assertEquals(0,
				JPA.em().createQuery("Select p from Project p", Project.class)
						.getResultList().size());

	}

	@Test
	public void testupdateProject() throws MalformedURLException {
		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));
		assertEquals("testProject", JPA.em().find(Project.class, 1).name);

		Project proj = JPA.em().find(Project.class, 1);
		proj.name = "newTestProject";
		JPA.em().persist(proj);

		assertEquals("newTestProject", JPA.em().find(Project.class, 1).name);

	}

	@Test
	public void testaddEmployeeToProject() throws MalformedURLException {
		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));

		Employee emp = new Employee("emp", "Employee");
		Project proj = JPA.em().find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		JPA.em().persist(proj);

		assertEquals(1, proj.projectEmployee.size());

	}

	@Test
	public void testdeleteEmployeeFromProject() throws MalformedURLException {
		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));

		Employee emp = new Employee("emp", "Employee");
		JPA.em().persist(emp);
		Project proj = JPA.em().find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		JPA.em().persist(proj);

		assertEquals(1, proj.projectEmployee.size());

		proj.projectEmployee.remove(emp);
		JPA.em().persist(proj);

		assertEquals(0, proj.projectEmployee.size());

	}

	@Test
	public void testupdateEmployeeInProject() throws MalformedURLException,
			ParseException {
		JPA.em()
				.persist(
						new Project("testProject", "TestDescription", new URL(
								"http://www.innoq.com"), false, new Date(),
								new Date()));

		Employee emp = new Employee("emp", "Employee");
		Project proj = JPA.em().find(Project.class, 1);
		proj.addEmployee(emp, new Date(), new Date());
		JPA.em().persist(proj);

		assertEquals(1, proj.projectEmployee.size());

		ProjectEmployee association = proj.projectEmployee.get(0);

		association.startDate = new SimpleDateFormat("MM/dd/yyyy")
				.parse("11/11/2014");
		JPA.em().persist(association);

		assertEquals(new SimpleDateFormat("MM/dd/yyyy").parse("11/11/2014"),
				proj.projectEmployee.get(0).startDate);

	}
}
