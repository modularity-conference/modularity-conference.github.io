// Copyright (c) 2007 Andrew Eisenberg
// Submission system for AOSD 2013 Industry track
// Redistributable under a BSD-style open source license.
// See http://www.linfo.org/bsdlicense.html

/*global $ document alert console window */
/*jslint browser:true */


if ($) {
	var MAX_CHARS = 12000;
	$(document).ready(function() {
	
		// sends the email
		function send() {
			// first gather the authors
			var authorRows = $('#authorsTable tr');
			var authors = [];
			for (var i = 0; i < authorRows.length; i++) {
				authors.push( {
					name: authorRows.find("#name"+ i).val(),
					email: authorRows.find("#email"+ i).val(),
					affiliation: authorRows.find("#affiliation"+ i).val()
				});
			}
			
			// create the to: and cc:
			var to = "industry@aosd.net";
			var cc = "";
			for (i = 0; i < authors.length; i++) {
				cc += authors[i].email;
				if (i < authors.length-1) {
					cc += ",";
				}
			}
			
			// create title
			var title = $("#title").val();
			var encodedtitle = encodeURIComponent(title);
			
			// create the subject
			var subject = "AOSD 2013 industry track submission: " + encodedtitle;
			
			// create the header to the message
			var message = "#<center>" + title + "</center>\n\n" +
				// use a table to put the authors in
				"<center><table>\n  <tr>";
				
			// table is centered, each author is a cell and each cell is centered
			for (i = 0; i < authors.length; i++) {
				message += "<td><center>" + 
					authors[i].name +"<br/>\n  " + 
					authors[i].email +"<br/>\n  " + 
					authors[i].affiliation + "</center></td>";
			}
			message += "</tr>\n</table></center>\n\n";
			message += $("#inputPane").val();
			
//			console.log("Unencoded message:");
//			console.log(message);
			
			// encode
			var encodedMessage = encodeURIComponent(message);
//			console.log("Encoded message:");
//			console.log(message);
			
			// put it all together
			var emailUri = "mailto:" + to + "?cc=" + cc + "&subject=" + subject + "&body=" + encodedMessage;

			// open email in default client so user can send
			window.open(emailUri);
			
			var lastName = authors[0].name.split(" ");
			lastName = lastName[lastName.length-1];
			if (!lastName) {
				lastName = authors[0].name;
			}
			
			// also ping the servers and store info there.
			$.post("http://static.springsource.org/aosd/submit.php", { author : lastName, submission : message }, 
				function(data, textStatus) {
					if (textStatus === 'success') {
						window.open("success.html");
					} else {
						alert("Error sending submission.  Reason: " + textStatus + "\n" +
								"Send by email instead or try with a more modern browser.");
					}
				}, "text");
			
		}
	
		// validations
		function validateEmail(str, idx) {
			if (!str) {
				return "Must include an email. ";
			}
		
			var at = "@";
			var dot = ".";
			var atIdx = str.indexOf(at);
			var strLen = str.length;
			var dotIdx = str.indexOf(dot);
			if (str.indexOf(at) === -1) {
				return "Invalid email address. ";
			}
		
			if (atIdx === -1 || atIdx === 0 || atIdx === strLen) {
				return "Invalid email address. ";
			}
		
			if (dotIdx === -1 || dotIdx === 0 || dotIdx === strLen) {
				return "Invalid email address. ";
			}
		
			if (str.indexOf(at, (atIdx + 1)) !== -1) {
				return "Invalid email address. ";
			}
		
			if (str.substring(atIdx - 1, atIdx) === dot || str.substring(atIdx + 1, atIdx + 2) === dot) {
				return "Invalid email address. ";
			}
		
			if (str.indexOf(dot, (atIdx + 2)) === -1) {
				return "Invalid email address. ";
			}
		
			// now check for undesired characters
			var notAllowed = " <>/\\()";
			for (var i = 0; i < notAllowed.length; i++) {
				if (str.indexOf(notAllowed.charAt(i)) !== -1) {
					return "Invalid email address. ";
				}
			}
		
			return "";
		}
		
		function validateName(name, idx) {
			if (!name) {
				return "Must include a name. ";
			} else {
				return "";
			}
		}
		
		function validateAffiliation(affiliation, idx) {
			if (!affiliation) {
				return "Must include an affiliation. ";
			} else {
				return "";
			}
		}
		
		function validateTitle(title) {
			if (!title) {
				return "Must include an title. ";
			} else {
				return "";
			}
		}
		
		// go through all fields and validate		
		function validateAll() {
			var result;
			// start with the title
			var title = $("#title").val();
			result = validateTitle(title);
			if (result) {
				result+= "\n";
			}
			
			var rows = $("table#authorsTable").find("tr");
			var lineRes, nameId, emailId, affiliationId;
			for (var idx = 0; idx < rows.length; idx++) {
				nameId = "name" + idx;
				emailId = "email" + idx;
				affiliationId = "affiliation" + idx;
				lineRes = "";
				lineRes += validateName($("#" + nameId).val(), idx);
				lineRes += validateEmail($("#" + emailId).val(), idx);
				lineRes += validateAffiliation($("#" + affiliationId).val(), idx);
				if (lineRes) {
					result += "  Author " + idx + ": " + lineRes + "\n";					
				}
			}
			if (idx < 1) {
				result = "  Must have at least one author. ";
			}
			
			var numChars = $("#inputPane").val().length;
			if (numChars > MAX_CHARS) {
				result += "\tSubmission is too long.  Max size is " + 
				MAX_CHARS + " characters, but you have " + numChars + " characters";
			}
			
			if (result) {
				result = "Please fix the following problems and resend:\n" + result;
			}
			
			return result;
		}
		


	
		// removes the last author from the list
		function removeAuthor() {
			$("table#authorsTable").find("tr").last().detach();
		}

		// adds a new author to the author's list
		function addAuthor() {
			// create the ids based on the current table size
			var cnt = $("table#authorsTable").find("tr").length;
			var nameId = "name" + cnt;
			var emailId = "email" + cnt;
			var affiliationId = "affiliation" + cnt;
			var nameImgId = "nameImg" + cnt;
			var emailImgId = "emailImg" + cnt;
			var affiliationImgId = "affiliationImg" + cnt;
			
			// create the new table row
			$("table#authorsTable").append(
				"<tr>\n" +
				"\t<td class='authorWord' >Author " + (cnt+1) + ":</td>\n" + 
				"\t<td class='authorTitle'>Full Name:</td>\n" + 
				"\t<td class='authorText' ><textarea cols='20' rows='1' id='" + nameId + "'/></td>\n" +
				"\t<td class='authorImg' id='"+ nameImgId + "' width='10'></td>\n" + 
				"\t<td class='authorTitle'>Email:</td>\n" +
				"\t<td class='authorText' ><textarea cols='20' rows='1' id='" + emailId + "'/></td>\n" +
				"\t<td class='authorImg' id='"+ emailImgId+ "' width='10'></td>\n" + 
				"\t<td class='authorTitle'>Affiliation</td>\n" +
				"\t<td class='authorText' ><textarea cols='20' rows='1' id='" + affiliationId + "'/></td>\n" +
				"\t<td class='authorImg' id='"+ affiliationImgId + "' width='10'></td>\n" + 
				"</tr>");
	
			// now attach validations to DOM nodes
			var nameTextArea = $("#" + nameId);
			nameTextArea.blur(
				function(e) {
					var res = validateName(nameTextArea.val(), cnt);
					if (res) {
						// add the error image
						$("#" + nameImgId).html("<img src='img/error.gif' title='" + res + "'/>");
					} else {
						// remove the error image
						$("#" + nameImgId).html("");
					}
				}
			); 
			
			var emailTextArea = $("#" + emailId);
			emailTextArea.blur(
				function(e) {
					var res = validateEmail(emailTextArea.val(), cnt);
					if (res) {
						// add the error image
						$("#" + emailImgId).html("<img src='img/error.gif' title='" + res + "'/>");
					} else {
						// remove the error image
						$("#" + emailImgId).html("");
					}
				}
			); 

			var affiliationTextArea = $("#" + affiliationId);
			affiliationTextArea.blur(
				function(e) {
					var res = validateAffiliation(affiliationTextArea.val(), cnt);
					if (res) {
						// add the error image
						$("#" + affiliationImgId).html("<img src='img/error.gif' title='" + res + "'/>");
					} else {
						// remove the error image
						$("#" + affiliationImgId).html("");
					}
				}
			); 
		}
		
		// validation for the title
		var titleTextArea = $("#title");
		var titleImg = $("#titleImg");
		titleTextArea.blur(
			function(e) {
				var res = validateTitle(titleTextArea.val());
				if (res) {
					// add the error image
					titleImg.html("<img src='img/error.gif' title='" + res + "'/>");
				} else {
					// remove the error image
					titleImg.html("");
				}
			}
		);
		
		// add behavior to the buttons
		$("button#addAuthorButton").click(addAuthor);
		$("button#removeAuthorButton").click(removeAuthor);
		$("button#submit").click(function() {
			var res = validateAll();
			return res ? alert(res) : send();
		});
		
		// keep track of number of characters in submission
		function setNumChars() {
			var numChars = $("#inputPane").val().length;
			var numCharsText = $("#numChars");
			numCharsText.text(MAX_CHARS - numChars);
			if (numChars > MAX_CHARS) {
				numCharsText.css('color', 'red');
			} else {
				numCharsText.css('color', 'black');
			} 
		}
		
		$("#inputPane").bind("keyup input paste", setNumChars);
		setNumChars();
		
		// popuate author table
		addAuthor();
	});
}

function checkemail(str) {
	if (!str) {
		return "Must include an email";
	}

	var at = "@";
	var dot = ".";
	var atIdx = str.indexOf(at);
	var strLen = str.length;
	var dotIdx = str.indexOf(dot);
	if (str.indexOf(at) === -1) {
		return "Invalid email address";
	}

	if (atIdx === -1 || atIdx === 0 || atIdx === strLen) {
		return "Invalid email address";
	}

	if (dotIdx === -1 || dotIdx === 0 || dotIdx === strLen) {
		return "Invalid email address";
	}

	if (str.indexOf(at, (atIdx + 1)) !== -1) {
		return "Invalid email address";
	}

	if (str.substring(atIdx - 1, atIdx) === dot || str.substring(atIdx + 1, atIdx + 2) === dot) {
		return "Invalid email address";
	}

	if (str.indexOf(dot, (atIdx + 2)) === -1) {
		return "Invalid email address";
	}

	if (str.indexOf(" ") !== -1) {
		return "Invalid email address";
	}

	return "";
}