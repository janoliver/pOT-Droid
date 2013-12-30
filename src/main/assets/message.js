$(document).ready(function() {

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
            api.displayBender(parseInt(user_id,10), "", 0);
        });
    }

    $("div.buttons.reply i.reply").click(function() {
        api.replyPost();
    });

});

function loadBender(user_id, path) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}