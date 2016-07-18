/*
 * PostgreSQL Studio
 */
package com.openscg.pgstudio.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.openscg.pgstudio.server.util.ConnectionInfo;
import com.openscg.pgstudio.server.util.ConnectionManager;

public class DataProxy extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
    		String clientIP = ConnectionInfo.remoteAddr(req);
            String userAgent = req.getHeader("User-Agent");
            String token = "";

        	HttpSession session = req.getSession(true);
        	token = (String) session.getValue("dbToken");

    		ConnectionManager connMgr = new ConnectionManager();
    		Connection conn = connMgr.getConnection(token,clientIP, userAgent);

    		String query = req.getParameter("query");
        	
        	
            String dir = "ASC".equals(req.getParameter("dir")) ? "+" : "-";
            String sort = req.getParameter("sort");
            String start_param = req.getParameter("start");
            int start = start_param == null ? 0 : Integer.parseInt(start_param);
            String limit_param = req.getParameter("limit");
            int limit = limit_param == null ? 100 : Integer.parseInt(limit_param);

            int total_count = 0;
            limit = Math.min(limit, total_count - start);

            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            List<JSONObject> items = new ArrayList<JSONObject>();
            
            ResultSetMetaData meta = rs.getMetaData();
            int size = meta.getColumnCount();
            
            while (rs.next()) {
            	total_count++;
                JSONObject item=new JSONObject();
            	
                for (int i = 1; i <= size; i++) {
            		String column = meta.getColumnName(i);
            		Object obj = rs.getObject(i);   
            		
            		if (obj != null) {
            			item.put(column, obj.toString());
            		}
            	}
            	items.add(item);
            }
            
            rs.close();
            stmt.close();
            
            JSONObject feeds = new JSONObject();
            JSONObject response = new JSONObject();
            feeds.put("response", response);

            response.put("data", items);
            response.put("total_count", Integer.toString(total_count));
            response.put("version", new Long(1)); 
            
            resp.setContentType("application/json; charset=utf-8");
            Writer w = new OutputStreamWriter(resp.getOutputStream(), "utf-8");
            w.write(feeds.toJSONString());
            w.close();
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            throw new IOException("Shouldn't happed");
        }
    }



}
