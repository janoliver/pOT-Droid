$(document).ready(function() {

    // bender loading
    if(api.isBenderEnabled() && api.downloadBenders()) {
        $(".bender:not([style])").each(function() {
            var bender = $(this).parents(".post").first();
            var user_id = bender.attr("data-user-id");
            var user_avatar_id = bender.attr("data-user-avatar-id");
            var user_avatar_file = bender.attr("data-user-avatar");
            api.displayBender(parseInt(user_id,10), user_avatar_file, parseInt(user_avatar_id,10));
        });
    }

});

// load the bender of user_id
function loadBender(user_id, path) {
    var el = $(".post[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}
