package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	public static Result projectOverview() {
		return ok(views.html.projectOverview.render());
	}

	public static Result userOverview() {
		return ok(views.html.userOverview.render());
	}

	public static Result projectView() {
		return ok(views.html.projectView.render());
	}

	public static Result projectEdit() {
		return ok(views.html.projectEdit.render());
	}

	public static Result projectEditAddUser() {
		return ok(views.html.projectEditAddUser.render());
	}

	public static Result userView() {
		return ok(views.html.userView.render());
	}
}
