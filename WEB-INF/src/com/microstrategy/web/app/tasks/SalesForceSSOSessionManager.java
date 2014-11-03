package com.microstrategy.web.app.tasks;

import java.util.HashMap;

import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class SalesForceSSOSessionManager {
	
	private static SalesForceSSOSessionManager _instance = null;
	private HashMap<String,JSONObject> _sessionList;
	
	private SalesForceSSOSessionManager(){
		_sessionList = new HashMap<String,JSONObject>();
	}
	
	public static SalesForceSSOSessionManager getInstance(){
		if(_instance == null){
			_instance = new SalesForceSSOSessionManager();
		}
		return _instance;
	}
	
	public HashMap<String,JSONObject> getSessionList(){
		return _sessionList;
	}
	
	public void setSessionList(HashMap<String,JSONObject> sessionList){
		_sessionList = sessionList;
	}

}
