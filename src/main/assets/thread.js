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

    // register waypoints while scrolling over them
    $("header").waypoint(function() {
        api.registerScroll(parseInt($(this).parent().attr("data-id"),10));
    });

    // scroll to the last post, when there was one
    if(api.getScroll() > 0) {
        document.location.href = "#" + api.getScroll();
    }

    // manual image loader
    $("div.img i").click(function() {
        replaceImage($(this));
    });

    $("div.spoiler i").click(function() {
        $(this).hide().parent().find('div').show();
    });

    // automatic image loader
    if(api.isLoadImages()) {
        var all = [];
        if(api.getScroll() > 0) {
            var self = $('a[name="' + api.getScroll() + '"]').parent().find('div.img i');
            var after = $('a[name="' + api.getScroll() + '"]').parent().nextAll().find('div.img i');
            //var before = $('a[name="' + api.getScroll() + '"]').parent().prevAll().find('div.img i');
            //all = $.merge($.merge(self,after), before);
            all = $.merge(self,after);
        } else {
            all = $('div.img i');
        }
        all.each(function() {
            replaceImage($(this));
        });
    }

    $('i.menu-icon').click(function() {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.openTopicMenu(post_id);
    });
});

function replaceImage(icon) {
    icon.attr('class', "icon-spinner spin");
    var el = icon.parent();
    var src = el.attr('data-src');
    var img = $('<img/>').attr('src',src).attr('alt',src);
    img.load(function() {
        el.replaceWith(img);
    });

}

// load the bender of user_id
function loadBender(user_id) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender")
      .css("background-image","url("+el.attr("data-user-avatar-path")+")");
}