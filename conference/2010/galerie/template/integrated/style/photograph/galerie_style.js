/*
 *	Style additionnel sur vignettes pour IE <= 7.0.
*/
function style_vignettes() {
	if (navigator.userAgent.search(/MSIE [1-7]/) != -1) {
		var tb = document.getElementById('vignettes');
		var tb_ex = document.getElementById('vex_vignettes');

		// Vignettes en mode 'compact'.
		if (tb) {
			var lis = tb.getElementsByTagName('li');
			var bc = (document.getElementById('vignettes_cat')) ? 0 : 1;
			for (var i = 0, l = lis.length; i < l; i++) {
				style_change_thumb(lis[i], bc);
			}

		// Vignettes en mode 'étendu'.
		} else if (tb_ex) {
			var divs = tb_ex.getElementsByTagName('div');
			for (var i = 0; i < divs.length; i++) {
				var cl = divs[i].className;
				if (cl && cl.search(/vex_vignette/gi) != -1) {

					// La souris survole la vignette.
					divs[i].getElementsByTagName('table')[0].onmouseover = function() {
						this.getElementsByTagName('tr')[0].getElementsByTagName('td')[1].style.backgroundColor = '#555555';
					};

					// La souris ne survole plus la vignette.
					divs[i].getElementsByTagName('table')[0].onmouseout = function() {
						this.getElementsByTagName('tr')[0].getElementsByTagName('td')[0].style.backgroundColor = '';
						this.getElementsByTagName('tr')[0].getElementsByTagName('td')[1].style.backgroundColor = '';
					};
				}
			}
		}

		// Vignette aléatoire.
		var has = document.getElementById('hasard_vignette');
		if (has) {
			style_change_thumb(has, 1);
		}
	}
}

// Vignettes en mode 'compact'.
function style_change_thumb(obj, bc) {
	var cl = obj.className;
	if (cl && cl.search(/v_thumb/gi) != -1) {

		// La souris survole la vignette.
		obj.onmouseover = function() {
			if (bc) {
				this.getElementsByTagName('span')[1].style.borderColor = '#7A7A7A';
			}
			this.getElementsByTagName('span')[1].style.backgroundColor = '#606060';
		};

		// La souris ne survole plus la vignette.
		obj.onmouseout = function() {
			if (bc) {
				this.getElementsByTagName('span')[1].style.borderColor = '#6A6A6A';
			}
			this.getElementsByTagName('span')[1].style.backgroundColor = '';
		};
	}
}
