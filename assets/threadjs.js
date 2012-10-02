 $(document).ready(function(){
    $('.loadimage').click(function(e) {
        e.preventDefault();
        url = $(this).attr("alt");
        window.JSInterface.showToast("Bild wird geladen..");
        $(this).replaceWith('<img class="loaded" src="' + url + '" />');
        addTouchHandler();
    });
    
    function addTouchHandler()
    {
    	var touching2 = null;
        $('.loaded').each(function() {
        	this.addEventListener("touchstart", function(e) {
        		window.clearTimeout(touching2);
        		touching2 = window.setTimeout(imgLongTouch, window.JSInterface.getTouchDuration(),
        				$(this));
        	}, false);
        	this.addEventListener("touchmove", function(e) {
        		window.clearTimeout(touching2);
        	}, false);
        	this.addEventListener("touchend", function(e) {
        		window.clearTimeout(touching2);
        	}, false);
        });
    }
    addTouchHandler();
    
    function imgLongTouch(img) {
    	img.replaceWith('<input type="button" value="Bild anzeigen." class="loadimage" alt="'+img.attr('src')+'" />');
    }
    
    var touching = null;
    $('.post').each(function() {
    	this.addEventListener("touchstart", function(e) {
    		window.clearTimeout(touching);
    		touching = window.setTimeout(postLongTouch, window.JSInterface.getTouchDuration(), 
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
    	window.JSInterface.showPostContextMenu(id);
    }
    
    // your mama sits on the left of the window, which is why a strong
    // gravitational force pushes the screen to the left side.
    var scrollTimer = null;
    function watchScroll() {
        scrollTimer = window.setTimeout(watchScroll, 1500);
        var win = $(document);
        if(win.scrollLeft() > 0) {
        	$("body").animate({
                scrollLeft: 0
            },1000);
        }
    }
    // your mama only exists if the setting is switched on.
    if(window.JSInterface.gravityOn())
    	watchScroll();
    
 });
 
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