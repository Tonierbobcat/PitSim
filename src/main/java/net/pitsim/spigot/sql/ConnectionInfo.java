package net.pitsim.spigot.sql;

import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.controllers.FirestoreManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

public enum ConnectionInfo {

	 PLAYER_DATA(
			 FirestoreManager.CONFIG.SQLDatabase,
			 FirestoreManager.CONFIG.SQLHostname,
			 FirestoreManager.CONFIG.SQLPort,
			 FirestoreManager.CONFIG.SQLUser,
			 FirestoreManager.CONFIG.SQLPassword,
			 1000L * 60 * 60 * 24 * 30
	 );

	 public final String DATABASE;
	 public final String HOSTNAME;
	 public final int PORT;
	 public final String USERNAME;
	 public final String PASSWORD;
	 public final long MAX_TIME;

	 ConnectionInfo(
			 String DATABASE,
			 String HOSTNAME,
			 int PORT,
			 String USERNAME,
			 String PASSWORD,
			 long MAX_TIME
	 ) {
		 this.HOSTNAME = HOSTNAME;
		 this.PORT = PORT;
		 this.USERNAME = USERNAME;
		 this.DATABASE = DATABASE;
		 this.PASSWORD = PASSWORD;
		 this.MAX_TIME = MAX_TIME;
	 }

	 public Connection getConnection() {
		 PitSim.INSTANCE.getLogger().log(Level.INFO, "Connecting to database...");
		 String url = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DATABASE + "?useSSL=false&serverTimezone=UTC";

		 Arrays.asList(
				 "database: " + DATABASE,
				 "hostname: " + HOSTNAME,
				 "port: " + PORT,
				 "user: " + USERNAME,
				 "pass: " + PASSWORD,
				 "full url: " + url
		 ).forEach(string -> {
			 PitSim.INSTANCE.getLogger().log(Level.INFO, "SQL " + string);
		 });
		 try {
			 Class.forName("com.mysql.jdbc.Driver");

			 return DriverManager.getConnection(url, USERNAME, PASSWORD);
		 } catch(ClassNotFoundException | SQLException e) {
			 PitSim.INSTANCE.getLogger().log(Level.SEVERE, "Database connection failed: " + e.getMessage());
			 return null;
		 }
     }
}
