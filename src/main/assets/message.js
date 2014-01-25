$(document).ready(function() {

    // bender loading
    if($(".bender").length > 0 && api.isBenderEnabled()) {

        if(api.getBenderPosition() == 1) {
            $("header .bender").show();
        } else if(api.getBenderPosition() == 2) {
            $("article .bender").show();
        } else if(api.getBenderPosition() == 3) {
            $("header .bender").addClass("portrait");
            $("article .bender").addClass("landscape");
        }

        $("section").each(function() {
            var bender = $(this);
            var user_id = bender.attr("data-user-id");
            var user_avatar_id = bender.attr("data-user-avatar-id");
            var user_avatar_file = bender.attr("data-user-avatar");
            api.displayBender(parseInt(user_id,10), user_avatar_file, parseInt(user_avatar_id,10));
        });
    }

});

// load the bender of user_id
function loadBender(user_id, path) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}
