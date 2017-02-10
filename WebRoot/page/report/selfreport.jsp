<%@ page language="java" pageEncoding="UTF-8"%>
<html>
	<title></title>
	<script src="/page/report/customres/flex/AC_OETags.js" language="javascript"></script>
	<script src="/page/report/customres/flex/history/history.js"	language="javascript"></script>				
	<link rel="stylesheet" type="text/css" href="/page/report/customres/flex/history/history.css" />
	<script language="JavaScript" type="text/javascript">
	var requiredMajorVersion = 9;
	var requiredMinorVersion = 0;
	var requiredRevision = 124;
	var hasProductInstall = DetectFlashVer(6, 0, 65);
	var hasRequestedVersion = DetectFlashVer(requiredMajorVersion,requiredMinorVersion, requiredRevision);

	if (hasProductInstall && !hasRequestedVersion) {
		var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
		var MMredirectURL = window.location;
		document.title = document.title.slice(0, 47) + " - Flash Player Installation";
		var MMdoctitle = document.title;
		AC_FL_RunContent(
				"src", "playerProductInstall",
				"width", "100%",
				"height", "100%",
				"align", "middle",
				"wmode","Opaque",
				"id", "selfReport",
				"quality", "high",
				"bgcolor", "#ffffff",
				"name", "selfReport",
				"allowScriptAccess","sameDomain",
				"type", "application/x-shockwave-flash",
				"pluginspage", "http://www.adobe.com/go/getflashplayer"
			);
		} else if (hasRequestedVersion) {
			AC_FL_RunContent(
					"src", "/page/report/ReportTemplate",
					"width", "100%",
					"height", "100%",
					"align", "middle",
					"wmode","Opaque",
					"id", "logicTopology",
					"quality", "high",
					"bgcolor", "#ffffff",
					"name", "logicTopology",
					"allowScriptAccess","sameDomain",
					"type", "application/x-shockwave-flash",
					"pluginspage", "http://www.adobe.com/go/getflashplayer"
					
			);
		  } else {  // flash is too old or we can't detect the plugin
		    var alternateContent = 'Alternate HTML content should be placed here. '
		  	+ 'This content requires the Adobe Flash Player. '
		   	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
		    document.write(alternateContent);  // insert non-flash content
		  }
	// -->
</script>
</html>