/*
Script: CountDown Timer
Description: Counts down or up from a date
Author: Andrew Urquhart
Home: http://www.andrewu.co.uk/clj/countdown/
History:
20040117 0035UTC	v1		Andrew Urquhart		Created
20040119 1943UTC	v1.1	Andrew Urquhart		Made more accessible/easier to use
20040317 1548UTC	v1.2	Andrew Urquhart		Implemented a less intrusive error message
20040331 1408BST	v1.3	Andrew Urquhart		Attempts to add to the currently window.onload schedule, rather than overriding it
*/
function CD_UpdateDisplay(strContent, strTagId) {
	var objHandle = document.getElementById(strTagId);
	if (objHandle && (typeof objHandle.innerHTML).toString().toLowerCase() != 'undefined') {
		objHandle.innerHTML = strContent;
	}
};

function CD_Tick(strTagId, strEventDate) {
	var objDateNow		= new Date();
	var intMsDelay		= 1000 - objDateNow.getMilliseconds();
	var intEventDate	= new Date(strEventDate).valueOf();
	CD_DrawTime(objDateNow, strTagId, intEventDate);
	setTimeout("CD_Tick(\"" + strTagId + "\", " + intEventDate + ")", intMsDelay);
};

function CD_DrawTime(strDate, strTagId, intEventDate) {
	var intMs = intEventDate - new Date(strDate).valueOf();
	if (intMs <= 0) {
		intMs = intMs * -1;
	}
	var intDays = Math.floor(intMs / 864E5);
	intMs = intMs - (intDays * 864E5);
	var intHours = Math.floor(intMs / 36E5);
	intMs = intMs - (intHours * 36E5);
	var intMinutes = Math.floor(intMs / 6E4);
	intMs = intMs - (intMinutes * 6E4);
	var intSeconds = Math.floor(intMs / 1000);
	var strTimeString = intDays + " day" + (intDays == 1 ? " " : "s ") + CD_ZP(intHours) + "h " + CD_ZP(intMinutes) + "m " + CD_ZP(intSeconds) + "s";
	CD_UpdateDisplay(strTimeString, strTagId);
};

function CD_ZP(objVal) {
	var str = "" + objVal;
	return (str.length < 2 ? "0" + str : str);
};

function CD_Init() {
	var strTagPrefix	= "countdown";
	var objHandle		= true; // temp value
	if (document.getElementById) {
		for (var i=1; objHandle; ++i) {
			var strElementName = strTagPrefix + i;
			objHandle	= document.getElementById(strElementName);

			if (objHandle && (typeof objHandle.innerHTML).toString().toLowerCase() != 'undefined') {
				var strDate	= objHandle.innerHTML;
				if (!isNaN(new Date(strDate))) {
					CD_Tick(strElementName, strDate);
					if (objHandle.style) {
						objHandle.style.visibility = "visible";
					}
				}
				else {
					objHandle.innerHTML = strDate + "<a href=\"http://www.andrewu.co.uk/clj/countdown/\" title=\"Countdown Error: Invalid date format used, check documentation (see link)\">*</a>";
				}
			}
		}
	}
};

// Try not to commandeer the default onload handler if possible
if (window.attachEvent) {
	window.attachEvent('onload', CD_Init);
}
else if (window.addEventListener) {
	window.addEventListener("load", CD_Init, false);
}
else {
	window.onload = CD_Init;
}
