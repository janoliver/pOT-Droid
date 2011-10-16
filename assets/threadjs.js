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
        		touching2 = window.setTimeout(imgLongTouch, 500, $(this));
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
    		touching = window.setTimeout(postLongTouch, 500, $(this).attr("id"));
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