package com.microstrategy.web.app.tasks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.utils.cache.ResourceBundleCache;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.RequestKeys;
import com.microstrategy.web.platform.ContainerServices;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskParameterMetadata;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.microstrategy.webapi.EnumDSSXMLAuthModes;

public class SalesForceSSOLoginTask extends GetSessionStateTask {

	// SalesForceSSO Params
	public static final String PARAM_NAME_API_KEY = "apiKey";
	public static final String PARAM_NAME_OAUTH_TOKEN = "oauthToken";
	public static final String PARAM_NAME_LOGIN_URL = "loginUrl";
	public static final String PARAM_NAME_ORGANIZATION_ID = "organizationId";
	public static final String PARAM_NAME_USER_ID = "userId";
	public static final String PARAM_NAME_NAMESPACE = "namespace";

	private TaskParameterMetadata loginUrlParam = addParameterMetadata(PARAM_NAME_LOGIN_URL, "The Salesforce login URL.", true, null);
	private TaskParameterMetadata namespaceParam = addParameterMetadata(PARAM_NAME_NAMESPACE, "The mstrwid.", true, null);

	public static final ResourceBundle APIKeys = ResourceBundle.getBundle("APIKeys");

	public SalesForceSSOLoginTask() {
		super();
	}

	@Override
	public void processRequest(TaskRequestContext paramTaskRequestContext, MarkupOutput paramMarkupOutput) throws TaskException {
		RequestKeys localRequestKeys = paramTaskRequestContext.getRequestKeys();

		// Add missing required parameters
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_SERVER, "DEFAULT");
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_PROJECT, "DEFAULT");

		checkForRequiredParameters(localRequestKeys);

		// Validate the apiKey (Extract connection information from the
		// properties file.
		ContainerServices containerServices = paramTaskRequestContext.getContainerServices();
		String apiKey = containerServices.getHeaderValue(PARAM_NAME_API_KEY);
		String connectionInfo = APIKeys.getString(apiKey);
		if (StringUtils.isEmpty(connectionInfo)) {
			throw new TaskException("The application is not authorized to create sessions.");
		}

		// Get Headers
		String oauthToken = containerServices.getHeaderValue(PARAM_NAME_OAUTH_TOKEN);
		String organizationId = containerServices.getHeaderValue(PARAM_NAME_ORGANIZATION_ID);
		String userId = containerServices.getHeaderValue(PARAM_NAME_USER_ID);
		String userName = containerServices.getHeaderValue(getSimpleSecurityLoginHeaderName());

		// Get parameters
		String loginUrl = this.loginUrlParam.getValue(localRequestKeys);
		String namespace = this.namespaceParam.getValue(localRequestKeys);

		// Extract SFDC userName with the oAuth Token
		String userNameFromSalesForce = getSalesforceUserName(oauthToken, loginUrl, organizationId, userId);
		if (StringUtils.isEmpty(userNameFromSalesForce)) {
			throw new TaskException("The oAuth token is not valid.");
		}
		if (!userNameFromSalesForce.equals(userName)){
			throw new TaskException("The oAuth token is not valid.");
		}

		// Check if there is an existing session for the oAuth token
		SalesForceSSOSessionManager sessionManager = SalesForceSSOSessionManager.getInstance();
		HashMap<String, JSONObject> sessionList = sessionManager.getSessionList();

		JSONObject sessionInfo = sessionList.get(namespace + ":" + oauthToken);
		if (sessionInfo != null) {
			String sessionState;
			try {
				sessionState = sessionInfo.getString("sessionState");
				localRequestKeys.add("oldSession", sessionState);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// remove previously added required parameters
		localRequestKeys.remove(GetSessionStateTask.PARAM_NAME_SERVER);
		localRequestKeys.remove(GetSessionStateTask.PARAM_NAME_PROJECT);

		// Add server, project, port and userName parameters
		String[] connectionProps = connectionInfo.split("[|]");
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_SERVER, connectionProps[0]);
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_PORT, connectionProps[1]);
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_PROJECT, connectionProps[2]);

		// Add authMode
		localRequestKeys.add(GetSessionStateTask.PARAM_NAME_AUTH_MODE, String.valueOf(EnumDSSXMLAuthModes.DssXmlAuthSimpleSecurityPlugIn));

		// Call the OOTB GetSessionStateTask
		paramTaskRequestContext.setRequestKeys(localRequestKeys);

		super.processRequest(paramTaskRequestContext, paramMarkupOutput);

		// Add/update the resulting sessionState into the Session Manager
		try {
			String response = paramMarkupOutput.getCopyAsString();
			InputStream responseBody = new ByteArrayInputStream(response.getBytes("UTF-8"));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(responseBody);
			Element element = doc.getDocumentElement();

			// Get the status
			if (element.getTagName().equals("taskResponse")) {
				String statusCode = element.getAttribute("statusCode");
				if (!statusCode.equals("200")) {
					return;
				}
			}

			// get the max-sessionstate
			NodeList elems = (NodeList) element.getElementsByTagName("max-state");
			String newSessionState = elems.item(0).getFirstChild().getNodeValue();

			sessionInfo = new JSONObject();
			sessionInfo.put("sessionState", newSessionState);
			String timeStamp = String.valueOf((new Date()).getTime());
			sessionInfo.put("timeStamp", timeStamp);

			sessionList = sessionManager.getSessionList();
			sessionList.remove(namespace + ":" + oauthToken);
			sessionList.put(namespace + ":" + oauthToken, sessionInfo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSalesforceUserName(String oauthToken, String loginUrl, String organizationId, String userId) {
		HttpClient httpclient = new HttpClient();
		GetMethod gm = new GetMethod(loginUrl + "/id/" + organizationId + "/" + userId);
		// set the token in the header
		gm.setRequestHeader("Authorization", "Bearer " + oauthToken);
		gm.setRequestHeader("X-PrettyPrint1", "1");
		try {
			httpclient.executeMethod(gm);
			InputStream response = gm.getResponseBodyAsStream();
			String responseBody = IOUtils.toString(response, "UTF-8");
			JSONObject json = new JSONObject(responseBody);
			String userName = json.getString("username");
			if (StringUtils.isNotEmpty(userName)) {
				return userName;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getSimpleSecurityLoginHeaderName() {
		ResourceBundle localResourceBundle = ResourceBundleCache.getBundle("resources.custom_security");
		return localResourceBundle.getString("LoginParam");
	}
}
