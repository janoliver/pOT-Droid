$(document).ready(function() {
    // bender loading
    if($(".bender").length > 0) {
        $("section").each(function() {
            var bender = $(this);
            var user_id = bender.attr("data-user-id");
            var user_avatar_id = bender.attr("data-user-avatar-id");
            var user_avatar_file = bender.attr("data-user-avatar");
            api.getBenderUrl(parseInt(user_id,10), user_avatar_file, parseInt(user_avatar_id,10));
        });
    }
});

function loadBender(user_id) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender")
      .css("background-image","url("+el.attr("data-user-avatar-path")+")");
}