Event.observe(window, "load", translate, false);

function translate() {
	/**
	 *  add caption to picbox from a[title] for lightbox
	 */
	$$("div.picbox > a[rel='lightbox']").each( function(a) {
		var p = new Element('p', {style: "width:" + a.clientWidth + "px;"}).update(a.getAttribute('title'));
		a.insert({after: p});
	});

	/**
	 *  make visible blockquote[cite]
     */
	$$("blockquote").each( function(bq) {
		var cite = bq.getAttribute('cite');
		if (cite != null && cite.length != 0) {
			var a = new Element('a', {href: cite}).update(cite);
			var p = new Element('p', {class: "cite"}).update("cited from ");
			p.insert({bottom: a});
			bq.insert({bottom: p});
		}
	});

	/**
	  * make visible e-mail address
	  */
	$$("a[rel='mailto']").each ( function(a) {
		adr = a.innerHTML.stripTags().evalJSON(true);
		adrStr = adr.name + "@" + adr.dom;
		a.setAttribute("href", "mailto:" + adrStr);
		a.update(adrStr);
	});
}


/* if animation is needed
Event.observe(window, "load", addMenuHandler, false);
function addMenuHandler() {
	$$('#menu dt a').each( function(e) {
		e.observe("mouseover", onMenuMouseOver, false);
	} );
}

function onMenuMouseOver(event) {
	var element = Event.element(event);
	var dt = element.up();
	var dl = dt.up();
	var dds = dl.getElementsByTagName("dd");
	if (dds.length != 0) {
		var dd = dds[0];
		dd.top = dt.top + dt.getHeight();
		dd.left = dt.left;
		dd.style.display = "block";
		dd.style.zIndex = "200";
		dd.show();		
	}
}
*/

/**
 * Top image cross fade
 */
Event.observe(window, "load", initTopImage, false);

function TopImage() {
	this.divs = $$('#topimage .crossfade');
	this.current = 0;
	this.isChanging = false;

	this.next = function() {
		var next = (this.current + 1) % this.divs.length;
		this.change(next);
	}

	this.change = function(next) {
		if (this.current != next && this.isChanging == false) {
			this.isChanging = true;
			this.divs[this.current].fade({duration: 1.5, from: 1.0, to: 0.0});
			this.divs[next].appear({duration: 1.5, from: 0.0, to: 1.0, afterFinish: this.changeFinish.bind(this)});
			this.current = next;
		}
	}
	this.changeFinish = function() {
		this.isChanging = false;
	}

	this.periodicalExec = new PeriodicalExecuter(this.next.bind(this), 5);
	this.resetTimeout = function() {
		this.periodicalExec.stop();
		this.periodicalExec.initialize(this.next.bind(this), 5);
	}
}

// global
var topImage = null;

function initTopImage() {
	if ($('topimage') != null) {
		topImage = new TopImage();
		$$('#topimage .thumb').each(function(thumb) {
			thumb.observe("click", selectThumb, false);
		});
	}
}

function selectThumb(event) {
	thumbs = $$('#topimage .thumb');
	for (var i = 0; i < thumbs.length; i++) {
		if (thumbs[i] == event.element()) {
			topImage.change(i);
			topImage.resetTimeout();
			break;
		}
	}
}
