/*
 * PostgreSQL Studio
 */
package com.openscg.pgstudio.server.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBVersionCheck {

	private final Connection conn;

	public enum PG_FLAVORS {
		STORMDB, OTHER_XC, POSTGRESQL, UNKNOWN
    }

	private PG_FLAVORS pgFlavor;

	public DBVersionCheck(Connection conn)	{
		this.conn = conn;
	}

	public boolean isStormDB() throws SQLException	{

		if(conn.getMetaData().getDatabaseProductVersion().contains("StormDB"))	{
			return true;
		}
		else
			return false;

	}

	public boolean isXC()	{

		boolean ret = false;

		try {
			if(!isStormDB())	{
				String test = "SELECT 1 from pg_catalog.pg_class where relname = 'pgxc_class';";
				PreparedStatement stmt = conn.prepareStatement(test);
				ResultSet rs = stmt.executeQuery();

				//DB is PostgreXC
				if(rs.next())	{
					ret = true;
				}

				//DB is Vanilla Postgres
				else	{
					ret = false;
				}

			}

			//DB is StormDB which is also XC
			else
				ret = true;

		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public PG_FLAVORS getPgFlavor()	{
		try {
			if(isStormDB())
				this.pgFlavor = PG_FLAVORS.STORMDB;
			else	{
				if(isXC())
					this.pgFlavor = PG_FLAVORS.OTHER_XC;
				else
					this.pgFlavor = PG_FLAVORS.POSTGRESQL;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pgFlavor;
	}
	
	public int getVersion() {
		int version = 0;
		String verStr = "";
		String query = "SELECT current_setting('server_version_num')";
		
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				verStr = rs.getString(1);								
				}

				try {
					version = Integer.parseInt(verStr);
				} catch (Exception e) {
					// This is an unknown version string format so just return 0
					version = 0;
				}
			}
		 catch (SQLException ex) {
			ex.printStackTrace();
		}
		return version;
	}

}