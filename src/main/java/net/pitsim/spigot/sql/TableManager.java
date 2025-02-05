package net.pitsim.spigot.sql;

import net.pitsim.spigot.PitSim;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TableManager {
	private static final List<SQLTable> tables = new ArrayList<>();

	public static boolean registerTables() {

		var info = ConnectionInfo.PLAYER_DATA;

        final java.sql.Connection connection;
        try {
            connection = info.getConnection();
			if (connection == null) {
				PitSim.INSTANCE.getLogger().log(Level.SEVERE, "Could not connection to SQL!");
				return false;
			}
        } catch (Exception e) {
			PitSim.INSTANCE.getLogger().log(Level.SEVERE, "Could not connection to SQL!");
			return false;
        }

		new SQLTable(info, "DiscordAuthentication",
				new TableStructure(
						new TableColumn(String.class, "uuid", false, true),
						new TableColumn(Long.class, "discord_id", true),
						new TableColumn(String.class, "access_token"),
						new TableColumn(String.class, "refresh_token"),
						new TableColumn(Long.class, "last_refresh", true),
						new TableColumn(Long.class, "last_link", true),
						new TableColumn(Long.class, "last_boosting_claim", true)
				));

		new SQLTable(info, "HelpRequests",
				new TableStructure(
						new TableColumn(String.class, "query", false, true, 255),
						new TableColumn(String.class, "intent", true)
				));
		return true;
	}

	protected static void registerTable(SQLTable table) {
		tables.add(table);
	}

	public static SQLTable getTable(String tableName) {
		for(SQLTable table : tables) {
			if(table.tableName.equals(tableName)) return table;
		}
		return null;
	}
}
