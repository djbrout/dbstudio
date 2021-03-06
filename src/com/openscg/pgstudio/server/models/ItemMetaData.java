/*
 * PostgreSQL Studio
 */
package com.openscg.pgstudio.server.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;

public class ItemMetaData {
	
	private final Connection conn;

	public ItemMetaData(Connection conn) {
		this.conn = conn;
	}
	
	public String getMetaData(int item, ITEM_TYPE type) throws SQLException {
		if (type != ITEM_TYPE.TABLE && type != ITEM_TYPE.VIEW && type != ITEM_TYPE.FOREIGN_TABLE) {
			return "";
		}

		Database db = new Database(conn);
		String name = db.getItemFullName(item, type);

		JSONArray ret = new JSONArray();		
		JSONObject result = new JSONObject();		
			
		String sql = "SELECT * FROM " + name + " LIMIT 0"; 
		
			try {
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				
				JSONArray metadata = new JSONArray();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					JSONObject jsonMessage = new JSONObject();
	
					jsonMessage.put("name", rs.getMetaData().getColumnName(i));
					jsonMessage.put("data_type", rs.getMetaData().getColumnType(i));
	
					metadata.add(jsonMessage);
				}			
				result.put("metadata", metadata);				
		} catch (SQLException e) {
			return "";
		} catch (Exception e) {
			return "";
		}
		
		ret.add(result);
		return ret.toString();

	}

}
