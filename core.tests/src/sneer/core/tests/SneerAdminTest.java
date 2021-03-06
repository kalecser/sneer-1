package sneer.core.tests;

import junit.framework.TestCase;

import sneer.PrivateKey;
import sneer.admin.SneerAdmin;

import java.io.File;
import java.io.IOException;

import static sneer.core.tests.ClojureUtils.dispose;
import static sneer.core.tests.ClojureUtils.var;

public class SneerAdminTest extends TestCase {

	public void testRememberKeys() throws IOException {

		File databaseFile = File.createTempFile("sneer", "db");

		Object db = produceDatabase(databaseFile);

		SneerAdmin admin = newSneerAdmin(db);

		PrivateKey privateKey = admin.privateKey();

		dispose(db);

		assertEquals(privateKey, newSneerAdmin(produceDatabase(databaseFile)).privateKey());

	}

	protected Object produceDatabase(File databaseFile) throws IOException {
		return var("sneer.tuple.jdbc-database", "create-sqlite-db").invoke(databaseFile);
	}

	private SneerAdmin newSneerAdmin(Object db) {
		return (SneerAdmin) ClojureUtils.var("sneer.admin", "new-sneer-admin-over-db").invoke(db);
	}

}
