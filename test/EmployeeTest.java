import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import models.Employee;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;

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

		new Employee("test", "TestEmployee").save();
		assertEquals(1, Ebean.find(Employee.class).findList().size());
		assertNotNull(Ebean.find(Employee.class, "test"));

	}
}
