$(document).ready(function(){

	// the image loader
	var resize = "";
	if(JSI.resizeImages()) {
		resize = 'resize';
		$("img").addClass(resize);
	}
	
	$('.loadimage').click(function(e) {
		e.preventDefault();
		url = $(this).attr("alt");
		JSI.showToast("Bild wird geladen..");
		$(this).replaceWith('<img class="' + resize + '" src="' + url + '" />');
	});
	 
	// this is for the context menu showing.
	var touching = null;
	$('.post').each(function() {
		this.addEventListener("touchstart", function(e) {
			window.clearTimeout(touching);
			touching = window.setTimeout(postLongTouch, JSI.getTouchDuration(), 
					$(this).attr("id"));
		}, false);
		this.addEventListener("touchmove", function(e) {
			window.clearTimeout(touching);
		}, false);
		this.addEventListener("touchend", function(e) {
			window.clearTimeout(touching);
		}, false);
	});
	
	function postLongTouch(id) {
		JSI.showPostContextMenu(id);
	}
	
	// spoilers.
	$("span.spoiler").hide()
		.before("<input type=\"button\" value=\"Spoiler\" class=\"spoiler\" /><br />");
	$("input.spoiler").on("click", function() {
		$(this).next().next("span.spoiler").toggle();
	});
	
	// your mom sits on the left of the window, which is why a strong
	// gravitational force pushes the screen to the left side.
	var scrollTimer = null;
	function watchScroll() {
		scrollTimer = window.setTimeout(watchScroll, 1500);
		var win = $(document);
		if(win.scrollLeft() > 0) 
			$("body").animate({scrollLeft: 0},1000);
	}
	
	// your mama only exists if the setting is switched on.
	if(JSI.gravityOn())
		watchScroll();

});

// scroll to some element with a given id.
function scrollToElement(id) {
    var elem = document.getElementById(id);
    var x = 0;
    var y = 0;

    while (elem != null) {
        x += elem.offsetLeft;
        y += elem.offsetTop;
        elem = elem.offsetParent;
    }
    window.scrollTo(x, y);
}