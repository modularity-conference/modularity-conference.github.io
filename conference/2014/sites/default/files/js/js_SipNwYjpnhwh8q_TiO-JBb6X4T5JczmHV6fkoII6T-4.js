jQuery(document).ready(function($) {	

	//Set Default State of each portfolio piece
	$(".paging").show();
	$(".paging a:first").addClass("active");

	//Get size of images, how many there are, then determin the size of the image reel.
	var imageWidth = $(".window").width();
	var imageSum = $(".image_reel img").size();
	var imageReelWidth = imageWidth * imageSum;
	
	//Adjust the image reel to its new size
	$(".image_reel").css({'width' : imageReelWidth});

	//Paging + Slider Function
	rotate = function(){	
	    var triggerID = $active.attr("rel") - 1; //Get number of times to slide
	    var image_reelPosition = triggerID * imageWidth; //Determines the distance the image reel needs to slide
	
	    $(".paging a").removeClass('active'); //Remove all active class
	    $active.addClass('active'); //Add active class (the $active is declared in the rotateSwitch function)
	    
		$(".desc").stop(true,true).slideUp('slow');
		
		$(".desc").eq( $('.paging a.active').attr("rel") - 1 ).slideDown("slow"); 
		
	    //Slider Animation
	    $(".image_reel").animate({ 
	        left: -image_reelPosition
	    }, 500 ); 
	
		
	}; 

	//Rotation + Timing Event
	rotateSwitch = function(){	
	$(".desc").eq( $('.paging a.active').attr("rel") - 1 ).slideDown("slow");	
	    play = setInterval(function(){ //Set timer - this will repeat itself every 3 seconds
	        $active = $('.paging a.active').next();
	        if ( $active.length === 0) { //If paging reaches the end...
	            $active = $('.paging a:first'); //go back to first
	        }
	        rotate(); //Trigger the paging and slider function
	    }, 10000); //Timer speed in milliseconds (3 seconds)	
	
	};
	
	rotateSwitch(); //Run function on launch

 //On Click
    $(".paging a").click(function() {    
        $active = $(this); //Activate the clicked paging
        //Reset Timer
        clearInterval(play); //Stop the rotation
        rotate(); //Trigger rotation immediately
        rotateSwitch(); // Resume rotation
        return false; //Prevent browser jump to link anchor
    });    

});
;
(function ($) {

  Drupal.behaviors.esi = {
    attach: function (context, settings) {
      var contextualize_urls = (typeof Drupal.settings.ESI !== 'undefined') && Drupal.settings.ESI.contextualize_URLs;
      if (contextualize_urls) {
        $().esiTags().DrupalESIContextualizeURL().handleESI();
      }
      else {
        $().esiTags().handleESI();
      }
    }
  };

  // Add the ESI context from the cookie to the ESI URL.
  $.fn.DrupalESIContextualizeURL = function() {
    // Regex to find the ESI context from an ESI src URL.
    var esi_context_regexp = /\/CACHE%3D([^\/]+)$/;

    $(this).each(function() {
      src = $(this).attr('src');
      
      // Get the context key from the ESI src attribute.
      // Contexts are provided in the last element: e.g. /CACHE=FOO.
      if (match = $(this).attr('src').match(esi_context_regexp)) {
        context_key = match[1];
        context_val = esi_get_user_context(context_key);
        new_src = src.replace(esi_context_regexp, '/CACHE=' + context_val);
        $(this).attr('src', new_src);
      }
    });
    return this;
  }

})(jQuery);

/**
 * Get the value of a particular context.
 */
function esi_get_user_context(context_key) {
  // Transform a context string (e.g. 'USER') into the actual cookie name.
  cookie_name = Drupal.settings.ESI.cookie_prefix + context_key + Drupal.settings.ESI.cookie_suffix;
  return esi_get_value_from_cookie(cookie_name);
}

/**
 * Get the value of a named cookie.
 */
function esi_get_value_from_cookie(cookie_name) {
  var all_cookies = document.cookie.split(';');
  for (var i=0; i < all_cookies.length; i++) {
    if (all_cookies[i].indexOf(cookie_name) === 1) {
      var values = all_cookies[i].split('=');
      return values[1];
    }
  }
  return '';
}
;
/**
 * jQuery ESI Plugin v1.0
 * http://github.com/manarth/jquery_esi
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 * Usage:
 * $().esiTags().handleESI();
 */

(function ($) {

  // Fetch all ESI tags.
  $.fn.esiTags = function() {
    // Initialise a jQuery collection to return.
    esi_tags = $();

    // The handler can be called as $().esiTags, or $('foo').esiTags().
    // Ensure we have an iterable collection in either instance.
    if (this.length == 0) {
      collection = $('html');
    }
    else {
      collection = this;
    }

    collection.each(function() {
      // Retrieve the base DOM element, in order to access the DOM method
      // getElementsByTagName.
      base_element = $(this).get(0);
      // Discover the <esi:include> tags.
      jQuery.each(base_element.getElementsByTagName('esi:include'), function(i, val) {
        // Some DOMs fail to recognise that the ESI include tag is self-
        // closing, so they treat following tags as child nodes.
        if ($(this).children().length > 0) {
          // Move the child nodes to become siblings.
          children = $(this).children().detach();
          $(this).after(children);
        }
        esi_tags = esi_tags.add($(this));
      });
      // Discover the <esi:remove> tags.
      jQuery.each(base_element.getElementsByTagName('esi:remove'), function(i, val) {
        // ESI remove tags are not self-closing, so no special treatment for
        // child nodes is needed.
        esi_tags = esi_tags.add($(this));
      });
    });
    return esi_tags;
  };

  // Move child nodes to siblings.
  $.fn.handleESIChildren = function() {
    $(this).each(function() {
    });
    return this;
  }

  // Handle ESI tags.
  // Delegates to either .handleESIInclude() or .handleESIRemove() as needed.
  $.fn.handleESI = function() {
    $(this).each(function() {
      switch (this.nodeName.toLowerCase()) {
        case 'esi:include':
          $(this).handleESIInclude();
          break;
      
        case 'esi:remove':
          $(this).handleESIRemove();
          break;
      }
    });

    return this;
  }

  // Handle a single ESI Include tag.
  $.fn.handleESIInclude = function() {
    src = $(this).attr('src');
    jQuery.ajax({
      context: this,
      url: src,
      success: function(data, textStatus, jqXHR) {
        esiElement = $(this);
        esiElement.replaceWith(data);
      }
    });
  }

  // Handle a single ESI Remove tag.
  $.fn.handleESIRemove = function() {
    $(this).replaceWith('');
  }

})(jQuery);
;
(function ($) {

$(document).ready(function() {

  // Accepts a string; returns the string with regex metacharacters escaped. The returned string
  // can safely be used at any point within a regex to match the provided literal string. Escaped
  // characters are [ ] { } ( ) * + ? - . , \ ^ $ # and whitespace. The character | is excluded
  // in this function as it's used to separate the domains names.
  RegExp.escapeDomains = function(text) {
    return (text) ? text.replace(/[-[\]{}()*+?.,\\^$#\s]/g, "\\$&") : '';
  }

  // Attach onclick event to document only and catch clicks on all elements.
  $(document.body).click(function(event) {
    // Catch the closest surrounding link of a clicked element.
    $(event.target).closest("a,area").each(function() {

      var ga = Drupal.settings.googleanalytics;
      // Expression to check for absolute internal links.
      var isInternal = new RegExp("^(https?):\/\/" + window.location.host, "i");
      // Expression to check for special links like gotwo.module /go/* links.
      var isInternalSpecial = new RegExp("(\/go\/.*)$", "i");
      // Expression to check for download links.
      var isDownload = new RegExp("\\.(" + ga.trackDownloadExtensions + ")$", "i");
      // Expression to check for the sites cross domains.
      var isCrossDomain = new RegExp("^(https?|ftp|news|nntp|telnet|irc|ssh|sftp|webcal):\/\/.*(" + RegExp.escapeDomains(ga.trackCrossDomains) + ")", "i");

      // Is the clicked URL internal?
      if (isInternal.test(this.href)) {
        // Is download tracking activated and the file extension configured for download tracking?
        if (ga.trackDownload && isDownload.test(this.href)) {
          // Download link clicked.
          var extension = isDownload.exec(this.href);
          _gaq.push(["_trackEvent", "Downloads", extension[1].toUpperCase(), this.href.replace(isInternal, '')]);
        }
        else if (isInternalSpecial.test(this.href)) {
          // Keep the internal URL for Google Analytics website overlay intact.
          _gaq.push(["_trackPageview", this.href.replace(isInternal, '')]);
        }
      }
      else {
        if (ga.trackMailto && $(this).is("a[href^=mailto:],area[href^=mailto:]")) {
          // Mailto link clicked.
          _gaq.push(["_trackEvent", "Mails", "Click", this.href.substring(7)]);
        }
        else if (ga.trackOutbound && this.href) {
          if (ga.trackDomainMode == 2 && isCrossDomain.test(this.href)) {
            // Top-level cross domain clicked. document.location is handled by _link internally.
            _gaq.push(["_link", this.href]);
          }
          else if (ga.trackOutboundAsPageview) {
            // Track all external links as page views after URL cleanup.
            // Currently required, if click should be tracked as goal.
            _gaq.push(["_trackPageview", '/outbound/' + this.href.replace(/^(https?|ftp|news|nntp|telnet|irc|ssh|sftp|webcal):\/\//i, '').split('/').join('--')]);
          }
          else {
            // External link clicked.
            _gaq.push(["_trackEvent", "Outbound links", "Click", this.href]);
          }
        }
      }
    });
  });
});

})(jQuery);
;
