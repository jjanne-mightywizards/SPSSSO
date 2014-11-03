<%@page import="main.java.microstrategy.sfdc.utils.*"%>
<% 
//String canvasRequestString = (String)request.getAttribute("canvasRequestJson");
String canvasRequestString = "{\"algorithm\":\"HMACSHA256\",\"issuedAt\":-72769329,\"userId\":\"005o0000000LdRfAAK\",\"client\":{\"refreshToken\":null,\"instanceId\":\"_:MicroStrategy_Canvas:j_id0:j_id5:canvasapp\",\"targetOrigin\":\"https://c.na17.visual.force.com\",\"instanceUrl\":\"https://na17.salesforce.com\",\"oauthToken\":\"00Do0000000aEfK!AQEAQDMLpmbsTR_A3O6aX_Dfwnar_3Hv3Ei9qH_LShx.6PLByUoGFoWxXnSmwHV4Rd0aH2WRhgmYlSly1WdltDmIeFpVMUjm\"},\"context\":{\"user\":{\"userId\":\"005o0000000LdRfAAK\",\"userName\":\"jjanne@mightywizards.com\",\"firstName\":\"Jesus\",\"lastName\":\"Janne\",\"email\":\"jjanne@mightywizards.com\",\"fullName\":\"Jesus Janne\",\"locale\":\"en_US\",\"language\":\"en_US\",\"timeZone\":\"America/Los_Angeles\",\"profileId\":\"00eo0000000uJTn\",\"roleId\":null,\"userType\":\"STANDARD\",\"currencyISOCode\":\"USD\",\"profilePhotoUrl\":\"https://c.na17.content.force.com/profilephoto/005/F\",\"profileThumbnailUrl\":\"https://c.na17.content.force.com/profilephoto/005/T\",\"siteUrl\":null,\"siteUrlPrefix\":null,\"networkId\":null,\"accessibilityModeEnabled\":false,\"isDefaultNetwork\":true},\"links\":{\"loginUrl\":\"https://login.salesforce.com/\",\"enterpriseUrl\":\"/services/Soap/c/32.0/00Do0000000aEfK\",\"metadataUrl\":\"/services/Soap/m/32.0/00Do0000000aEfK\",\"partnerUrl\":\"/services/Soap/u/32.0/00Do0000000aEfK\",\"restUrl\":\"/services/data/v32.0/\",\"sobjectUrl\":\"/services/data/v32.0/sobjects/\",\"searchUrl\":\"/services/data/v32.0/search/\",\"queryUrl\":\"/services/data/v32.0/query/\",\"recentItemsUrl\":\"/services/data/v32.0/recent/\",\"chatterFeedsUrl\":\"/services/data/v31.0/chatter/feeds\",\"chatterGroupsUrl\":\"/services/data/v32.0/chatter/groups\",\"chatterUsersUrl\":\"/services/data/v32.0/chatter/users\",\"chatterFeedItemsUrl\":\"/services/data/v31.0/chatter/feed-items\",\"userUrl\":\"/005o0000000LdRfAAK\"},\"application\":{\"namespace\":null,\"name\":\"MicroStrategy Canvas\",\"canvasUrl\":\"https://microstrategycanvas.herokuapp.com/canvas\",\"applicationId\":\"06Po00000000Pnf\",\"version\":\"1.0\",\"authType\":\"SIGNED_REQUEST\",\"referenceId\":\"09Ho00000008nqQ\",\"options\":[],\"samlInitiationMethod\":\"None\",\"developerName\":\"MicroStrategy_Canvas\"},\"environment\":{\"locationUrl\":\"https://c.na17.visual.force.com/apex/POS_System?sfdc.tabName=01ro0000000ATDb\",\"displayLocation\":\"Visualforce\",\"sublocation\":null,\"uiTheme\":\"Theme3\",\"dimensions\":{\"width\":\"100%\",\"height\":\"800px\",\"maxWidth\":\"1000px\",\"maxHeight\":\"2000px\",\"clientWidth\":\"1660px\",\"clientHeight\":\"30px\"},\"parameters\":{\"webUrl\":\"http://localhost:8080/MicroStrategy/servlet/mstrWeb\",\"server\":\"MAC-AJANNE1VM\",\"port\":\"0\",\"project\":\"MicroStrategy Tutorial\",\"documentID\":\"B1171E034B69E3689FE15487A59883A2\",\"mstrwid\":\"container2\"},\"record\":{},\"version\":{\"season\":\"WINTER\",\"api\":\"32.0\"}},\"organization\":{\"organizationId\":\"00Do0000000aEfKEAU\",\"name\":\"Mighty Wizards\",\"multicurrencyEnabled\":false,\"namespacePrefix\":null,\"currencyIsoCode\":\"USD\"}}}";
String sessionState = SalesforceSSOUtils.getMSTRSessionState(canvasRequestString);
%>
<html>
<head>
<title></title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script>
	debugger;
	var sessionState = "<%=sessionState.replace("\"", "\\\"")%>";
	//Sfdc.canvasRequest = JSON.parse('${canvasRequestJson}');
	var Sfdc = {};
	Sfdc.canvasRequest = JSON.parse('<%=canvasRequestString%>');

	// MicroStrategy parameters
	var mstr = {
		canvasOptions : {
			width : "100%",
			height : "800px"
		},
		webUrl : "https://192.168.1.28/MicroStrategy/asp/Main.aspx",
		urlParams : {
			evt : "2048001",
			src : "Main.aspx.2048001",
			hiddenSections : "header,path,dockTop,dockLeft,dockBottom,footer"
		}
	};

	var constructIframeUrl = function() {
		return mstr.webUrl + "?" + jQuery.param(mstr.urlParams);
	};

	var updateMstrCanvasIframeSRC = function() {
		var url = constructIframeUrl();
		$('#mstrCanvasIframe').attr('src', url);
	};

	var submitMstrUrlToIfame = function() {
		var form = $("<form/>", {
			action : mstr.webUrl,
			method : '#',
			target : 'mstrCanvasIframe'
		});
		for ( var key in mstr.urlParams) {
			form.append($("<input/>", {
				type : 'hidden',
				name : key,
				value : mstr.urlParams[key]
			}));
		}
		$("#content").append(form);
		form.submit();
	};

// 	Sfdc.canvas(function() {
	window.onload = function(){
		if (Sfdc.canvasRequest.context.environment.parameters.webUrl) {
			//Get the MicroStrategy Web URL
			mstr.webUrl = Sfdc.canvasRequest.context.environment.parameters.webUrl;
			//Get the connection parameters
			mstr.urlParams.usrSmgr = sessionState;
			mstr.urlParams.documentID = Sfdc.canvasRequest.context.environment.parameters.documentID;
			mstr.urlParams.mstrwid = Sfdc.canvasRequest.context.environment.parameters.mstrwid;
		}
		// Construct the url and set it to the Iframe.
		//updateMstrCanvasIframeSRC();
		submitMstrUrlToIfame();
// 	});
	};
</script>
</head>
<body>
	<div id="content">
		<!--
        <div id="header">
            <h1>Hello <span id='fullname'>${canvasRequest.context.userContext.fullName}</span>!</h1>
            <h2>Welcome to the Force.com Canvas Java Quick Start Template!</h2>
        </div>
	-->
		<iframe id="mstrCanvasIframe" name="mstrCanvasIframe" src="" style="border: 0px; margin: 0px; padding: 0px; height: 100%; width: 100%;" height="100%" width="100%"></iframe>
	</div>
</body>
</html>