$(document).ready(function() {
    // bender loading
    if($(".bender").length > 0) {
        $("section").each(function() {
            var bender = $(this);
            var user_id = bender.attr("data-user-id");
            var user_avatar_id = bender.attr("data-user-avatar-id");
            var user_avatar_file = bender.attr("data-user-avatar");
            api.displayBender(parseInt(user_id,10), user_avatar_file, parseInt(user_avatar_id,10));
        });
    }

    // login shit
    if(!api.isLoggedIn()) {
        $(".login").hide();
    }

    // scroll to the last post, when there was one
    if(api.getScroll() > 0) {
        document.location.href = "#" + api.getScroll();
        var before = $('a[name="' + api.getScroll() + '"]').parent().prevAll();
        before.css({ opacity: 0.5 });
    }

    // automatic image loader
    // Only images within or after the currently visible post are loaded, so
    // the visible position is not changed
    // also, we don't really need to see the pictures above.
    if(api.isLoadImages()) {
        var all = [];
        if(api.getScroll() > 0) {
            var self = $('a[name="' + api.getScroll() + '"]').parent().find('div.img i, div.img-link i.img');
            var after = $('a[name="' + api.getScroll() + '"]').parent().nextAll().find('div.img i, div.img-link i.img');
            all = $.merge(self, after);
        } else {
            all = $('div.img i, div.img-link i.img');
        }
        all.each(function() {
            var href = $(this).parent().attr("data-href");
            if(typeof href === "undefined") {
                replaceImage($(this), "");
            } else {
                replaceImage($(this), href);
            }

        });
    }

    // manual image loader
    $("div.img i").click(function() {
        replaceImage($(this), "");
    });

    // manual image with link loader
    $("div.img-link i.link").click(function() {
        api.openUrl($(this).parent().attr('data-href'));
    });

    // manual image with link loader
    $("div.img-link i.img").click(function() {
        replaceImage($(this), $(this).parent().attr('data-href'));
    });

    $("div.spoiler i").click(function() {
        $(this).hide().parent().find('div').show();
    });

    $('i.menu-icon').click(function(e) {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.openTopicMenu(post_id);
    });

    $('i.menu-edit').click(function(e) {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.editPost(post_id);
    });

    $('i.menu-quote').click(function(e) {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.quotePost(post_id);
    });

    $('i.menu-bookmark').click(function(e) {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.bookmarkPost(post_id);
    });

    $('i.menu-link').click(function(e) {
        var post_id = parseInt($(this).closest('section').attr('data-id'));
        api.linkPost(post_id);
    });

    $("div.buttons.reply i.reply").click(function() {
        api.replyPost();
    });

    $("div.buttons.paginate i.frwd").click(function() {
        api.frwd();
    });

    $("div.buttons.paginate i.rwd").click(function() {
        api.rwd();
    });

    $("div.buttons.paginate i.refresh").click(function() {
        api.refresh();
    });

    $("div.buttons.paginate i.fwd").click(function() {
        api.fwd();
    });

    $("div.buttons.paginate i.ffwd").click(function() {
        api.ffwd();
    });

    // hide the paginate buttons
    if(api.isLastPage()) {
        $("div.buttons.paginate i.fwd, div.buttons.paginate i.ffwd").css('visibility','hidden');
    }
    if(api.isFirstPage()) {
        $("div.buttons.paginate i.rwd, div.buttons.paginate i.frwd").css('visibility','hidden');
    }

    // register waypoints while scrolling over them
    // should be the last thing executed!
    $("header").waypoint(function() {
        api.registerScroll(parseInt($(this).parent().attr("data-id"),10));
    });
});

function replaceImage(icon, link_target) {
    icon.attr('class', "fa fa-spinner spin");
    var el = icon.parent();
    var src = el.attr('data-src');
    var img = $('<img/>').attr('src',src).attr('alt',src);
    img.load(function() {
        if(link_target === "") {
            el.replaceWith(img);
        } else {
            var a = $("<a/>").attr("href", link_target).append(img);
            el.replaceWith(a);
        }

    });
}

// load the bender of user_id
function loadBender(user_id, path) {
    var el = $("section[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}