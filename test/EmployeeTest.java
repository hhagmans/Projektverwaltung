import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import models.Employee;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

public class EmployeeTest extends WithApplication {

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
	public void testaddEmployee() {

		JPA.em().persist(new Employee("test", "TestEmployee", false));
		assertEquals(
				1,
				JPA.em()
						.createQuery("Select e from Employee e", Employee.class)
						.getResultList().size());
		assertNotNull(JPA.em().find(Employee.class, "test"));

	}
}
