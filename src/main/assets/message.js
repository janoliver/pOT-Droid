$(document).ready(function() {
    if($(".bender").length > 0) {
        $("section").each(function() {
            var bender = $(this);
            var user_id = bender.attr("data-user-id");
            api.displayBender(parseInt(user_id,10), "", 0);
        });
    }

});

function loadBender(user_id, path) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}