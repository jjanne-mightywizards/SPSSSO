package com.microstrategy.web.app.tasks;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

/**
 * Handles session reuse and maintenance
 */
public class SPSSSOSessionManager {

	private static SPSSSOSessionManager _instance = null;
	private HashMap<String, JSONObject> _sessionList;
	private Date _lastCleanupDate;
	private boolean _cleanupInProgress;

	/**
	 * Constructor
	 */
	private SPSSSOSessionManager() {
		_sessionList = new HashMap<String, JSONObject>();
		_lastCleanupDate = new Date();
		_cleanupInProgress = false;
	}

	/**
	 * Gets the singleton instance
	 * @return
	 */
	public static SPSSSOSessionManager getInstance() {
		if (_instance == null) {
			_instance = new SPSSSOSessionManager();
		}
		return _instance;
	}

	/**
	 * Gets the session list
	 * @return
	 */
	public HashMap<String, JSONObject> getSessionList() {
		return _sessionList;
	}

	/**
	 * Sets the session list
	 * @param sessionList
	 */
	public void setSessionList(HashMap<String, JSONObject> sessionList) {
		_sessionList = sessionList;
	}

	/**
	 * Executes the clean up process
	 */
	public void executeCleanUp() {
		if(_cleanupInProgress){
			return;
		}
		_cleanupInProgress = true;
		Date currentDate = new Date();
		int days = Days.daysBetween(new DateTime(_lastCleanupDate), new DateTime(currentDate)).getDays();
		if (days > 0) {
			try {
				cleanupSessions(currentDate);
				_lastCleanupDate = currentDate;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		_cleanupInProgress = false;
	}

	/**
	 * Removes all sessions stored for longer than 1 day
	 * @param currentDate
	 * @throws JSONException
	 */
	public void cleanupSessions(Date currentDate) throws JSONException {
		Iterator<Entry<String, JSONObject>> it = _sessionList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, JSONObject> pairs = (Map.Entry<String, JSONObject>) it.next();
			JSONObject sessionInfo = pairs.getValue();
			Date timeStamp = (Date) sessionInfo.get("timeStamp");
			int days = Days.daysBetween(new DateTime(timeStamp), new DateTime(currentDate)).getDays();
			if (days > 0)
				it.remove();
		}
	}

}
