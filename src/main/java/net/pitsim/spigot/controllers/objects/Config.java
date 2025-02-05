package net.pitsim.spigot.controllers.objects;

import com.google.cloud.firestore.annotation.Exclude;
import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.controllers.FirestoreManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Config {
	@Exclude
	public boolean onSaveCooldown = false;
	@Exclude
	public boolean saveQueued = false;

	public String prefix = "";
	public String errorPrefix = "&c";
	public boolean nons = true;
	public String mapData;

	public Map<String, String> boosterActivatorMap = new HashMap<>();
	public Map<String, Integer> boosters = new HashMap<>();

	//	PitSim pass stuff
	public Date currentPassStart;
	public CurrentPassData currentPassData;


	public String SQLHostname;
	public String SQLUser;
	public String SQLPassword;
	public String SQLDatabase;
	public int SQLPort;

	//	public Security security = new Security();
//
//	public static class Security {
//		public boolean requireVerification = false;
//		public boolean requireCaptcha = false;
//	}

//	public String pteroURL = null;
//	public String pteroClientKey = null;



//	public String alertsWebhook = null;
//	public String bansWebhook = null;

	public static class CurrentPassData {
		public Map<String, Integer> activeWeeklyQuests = new HashMap<>();
	}



	public Config() {}

	@Exclude
	public void save() {
//		if(!PitSim.serverName.equals("pitsim-1") && !PitSim.serverName.equals("pitsimdev-1")) return;
		if(onSaveCooldown && !saveQueued) {
			saveQueued = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				saveQueued = false;
				save();
			}).start();
		}
		if(!saveQueued && !onSaveCooldown) {
			FirestoreManager.FIRESTORE.collection(FirestoreManager.SERVER_COLLECTION).document(FirestoreManager.CONFIG_DOCUMENT).set(this);
			AOutput.log("Saving PitSim Config");
			onSaveCooldown = true;
			new Thread(() -> {
				try {
					Thread.sleep(1500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				onSaveCooldown = false;
			}).start();
		}
	}

	@Exclude
	public void load() {
		try {
			var conf = FirestoreManager.FIRESTORE.collection(FirestoreManager.SERVER_COLLECTION)
					.document(FirestoreManager.CONFIG_DOCUMENT).get().get().toObject(Config.class);

			var config = PitSim.INSTANCE.getConfig();

			this.SQLDatabase = config.getString("mysql.database");
			this.SQLHostname = config.getString("mysql.hostname");
			this.SQLPort = config.getInt("mysql.port");
			this.SQLUser = config.getString("mysql.user");
			this.SQLPassword = config.getString("mysql.password");

			this.prefix = config.getString("prefix");
			this.errorPrefix = config.getString("error-prefix");
			conf = this;
			FirestoreManager.CONFIG = conf;

			FirestoreManager.CONFIG.save();
//			if (conf == null) {
//				PitSim.INSTANCE.getLogger().log(Level.WARNING, "Config is null! Initializing config.");
//				var config = PitSim.INSTANCE.getConfig();
//				this.SQLHostname = config.getString("sql.hostname");
//				this.SQLPassword = config.getString("sql.password");
//				this.SQLUser = config.getString("sql.user");
//				this.prefix = config.getString("prefix");
//				this.errorPrefix = config.getString("error-prefix");
//				conf = this;
//				FirestoreManager.CONFIG = conf;
//			}
//			else {
//				FirestoreManager.CONFIG = conf;
//				PitSim.INSTANCE.getLogger().log(Level.WARNING, "Successfully loaded config!");
//			}
		} catch(InterruptedException | ExecutionException exception) {
			PitSim.INSTANCE.getLogger().log(Level.SEVERE, "Could not load config! " + exception.getMessage());
		}
	}
}
