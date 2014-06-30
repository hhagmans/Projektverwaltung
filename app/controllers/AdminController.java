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
import java.util.ArrayList;
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
import play.data.DynamicForm;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class AdminController extends Controller {

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

	/**
	 * Verschlüsselt das im Admin Menü angegebene Passwort
	 * 
	 * @param password
	 * @return
	 */
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

	/**
	 * Entschlüsselt das von encryptPassword verschlüsselte Passwort.
	 * 
	 * @param password
	 * @return
	 */
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
	 * Prüft mittels LiQID, ob der Mitarbeiter Principal Consultant ist.
	 * 
	 * @param emp
	 * @return
	 */
	public static boolean checkIsPrincipal(String id) {
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
			Iterator<JsonNode> iter = json.get("groups").iterator();
			while (iter.hasNext()) {
				JsonNode currentNode = iter.next();
				if (currentNode.get("uid").textValue().equals("Principals")) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return false;
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
				boolean isPrincipal = checkIsPrincipal(id);
				if (!id.equals("admin") && !id.equals("csarazin")
						&& !id.equals("pppreader"))
					list.add(new Employee(id, name, isPrincipal));
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
			boolean isPrincipal = checkIsPrincipal(id);
			return new Employee(id, name, isPrincipal);
		} catch (IOException e) {
			e.printStackTrace();

			return null;
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
	@Transactional
	public static boolean synchronizeDB(String name, String password) {
		List<Employee> allEmps = getAllEmployees(name, password);
		if (allEmps != null) {
			for (Employee employee : allEmps) {
				JPA.em().persist(employee);
			}
			return true;
		} else {
			return false;
		}
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
	@Transactional
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
