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

    // set top and bottom padding
    if(api.isOverlayToolbars()) {
        $("#paddings").css("padding-top", api.getToolBarHeightInDp() + "px");
        if(api.isBottomToolbar())
            $("#paddings").css("padding-bottom", api.getToolBarHeightInDp() + "px");
    }

    if(!api.isPullUpToRefresh() || !api.isShowEndIndicator() || !api.isLastPage()) {
        $(".up-indicator").hide();
    }

    // automatic image loader
    // Only images within or after the currently visible post are loaded, so
    // the visible position is not changed
    // also, we don't really need to see the pictures above.
    if(api.isLoadImages()) {
        loadAllImages();
    }

    if(api.isLoadGifs()) {
        loadAllGifs();
    }

    if(api.isLoadVideos()) {
        loadAllVideos();
    }

    // manual image loader
    $("div.img.media, div.img-link.media").on("click", "button.viewer", function() {
        api.zoom($(this).parent().attr('data-src'), "image");
    });

    $("div.gif.media, div.gif-link.media").on("click", "button.viewer", function() {
        api.zoom($(this).parent().attr('data-src'), "gif");
    });

    // manual image loader
    $("div.video.media").on("click", "button.viewer", function() {
        api.zoom($(this).parent().attr('data-src'), "video");
    });

    // manual video loader
    $("div.video.media").on("click", "button.inline", function() {
        replaceVideo($(this).parent());
    });

    // manual video loader
    $("div.video.media").on("click", "button.link", function() {
        api.openUrl($(this).parent().attr('data-src'));
    });

    // manual image with link loader
    $("div.img-link.media, div.gif-link.media").on("click", "button.link", function() {
        api.openUrl($(this).parent().attr('data-href'));
    });

    // manual image with link loader
    $("div.img-link.media, div.gif-link.media, div.img.media, div.gif.media").on(
        "click", "button.inline", function() {
        replaceImage($(this).parent());
    });

    $("div.spoiler button").click(function() {
        var p = $(this).parent();
        p.find("button,i").hide();
        p.css("display", "block");
        p.find('div.spoiler-content').show();
    });

    $('.post-menu-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.openTopicMenu(post_id);
    });

    $('.edit-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.editPost(post_id);
    });

    $('.quote-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.quotePost(post_id);
    });

    $('.bookmark-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.bookmarkPost(post_id);
    });

    $('.link-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.linkPost(post_id);
    });

    $('.copy-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.copyPostLink(post_id);
    });

    $('.pm-button').click(function(e) {
        var post_id = parseInt($(this).closest('.post').attr('data-id'));
        api.pmAuthor(post_id);
    });

    $('a.author').click(function(e) {
        if($("a[name=" + getURLParameter(this, 'PID') + "]").length > 0) {
            e.preventDefault();
            location.hash = "#" + getURLParameter(this, 'PID');
        }
    });

    // scroll to the last post, when there was one
    // to ensure correct scrolling, this should be the last JS call.
    if(api.getScroll() > 0) {
        var scrollto = $('a[name="' + api.getScroll() + '"]');
        if(api.isDarkenOldPosts()) {
            scrollto.parent().prevAll().addClass("oldpost");
        }
        if(api.isMarkNewPosts()) {
            scrollto.parent().nextAll().addClass("newpost");
        }
    }

    setTimeout(function() {
        if(api.getScroll() > 0) {
            $('html, body').scrollTop(scrollto.offset().top - api.getToolBarHeightInDp());
        } else {
            window.scrollTo(0,0);
        }

        changeConfiguration(api.isLandscape());

        // register waypoints while scrolling over them
        // should be the last thing executed!
        var continuousElements = document.getElementsByClassName('header')
        for (var i = 0; i < continuousElements.length; i++) {
            new Waypoint({
                element: continuousElements[i],
                handler: function() {
                    api.registerScroll(parseInt($(this.element).parent().attr("data-id"),10));
                }
            })
        }

    }, 100);
});

function replaceImage(container) {
    var classes = "material-icons img-spinner spin";
    var content = "&#xE028;";
    var icon = container.find("i");
    icon.attr('class', classes);
    icon.html(content);

    var src = container.attr('data-src');
    var id = Math.floor( Math.random()*99999 );
    container.attr("id", "loadedimg-" + id);
    api.loadImage(src, container.attr("id"));
}

function displayImageLoader(id) {
    var el = $("#" + id);
    var icon = el.find("i");
    var classes = "material-icons err";
    var content = "&#xE410;";
    if(icon.parent().hasClass("gif"))
        content = "&#xE54D;";
    icon.attr("class", classes);
    icon.html(content);
    el.removeAttr('id');
}

function displayImage(url, path, id) {
    var el = $("#" + id);
    var href = el.attr("data-href");
    var img = $('<img/>').attr('src', path).attr('alt', url);
    if(typeof href === "undefined") {
        el.replaceWith(img);
    } else {
        var a = $("<a/>").attr("href", href).append(img);
        el.replaceWith(a);
    }
}

function youtube_parser(url){
    var regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
    var match = url.match(regExp);
    if (match&&match[2].length==11){
        return match[2];
    } else {
        // error
    }
}

function replaceVideo(el) {
    var src = el.attr('data-src');

    if(el.hasClass("yt")) {
        var id = youtube_parser(src);
        var wrapper = $("<div class=\"videoWrapper\"></div>");
        var iframe = $("<iframe type=\"text/html\" frameborder=\"0\" allowfullscreen></iframe>")
            .attr("src", "http://www.youtube.com/embed/" + id + "?fs=1");
        wrapper.append(iframe);
        el.replaceWith(wrapper);
    } else {
        var wrapper = $('<video controls width="100%"></video>').attr('src', src);
        el.replaceWith(wrapper);
    }

}

// load the bender of user_id
function loadBender(user_id, path) {
    var el = $(".post[data-user-id='"+user_id+"']");
    el.find("div.bender").css("background-image","url("+path+")");
}

// unveil dimmed posts
function unveil() {
    $("*").css({ opacity: 1.0 });
    $('html, body').scrollTop( 0 );
}

// unveil dimmed posts
function scrollToTop() {
    $('html, body').scrollTop( 0 );
}

// unveil dimmed posts
function scrollToBottom() {
    $('html, body').scrollTop( $(document).height() );
}

// scroll to the last post of UID uid
function scrollToLastPostByUID(uid) {
    var a = $(".post[data-user-id="+uid+"]").last().find("a").first();
    var href = a.attr("name");
    if(typeof href === "undefined")
        api.error("Kein Post auf dieser Seite");
    else
        $('html, body').scrollTop(a.offset().top + api.getToolBarHeightInDp());
}

// load all images
function loadAllImages() {
    var all = [];
    if(api.getScroll() > 0) {
        var self = $('a[name="' + api.getScroll() + '"]').parent().find('div.img.media, div.img-link.media');
        var after = $('a[name="' + api.getScroll() + '"]').parent().nextAll().find('div.img.media, div.img-link.media');
        all = $.merge(self, after);
    } else {
        all = $('div.img.media, div.img-link.media');
    }
    all.each(function() {
        replaceImage($(this));
    });
}

// load all images
function loadAllGifs() {
    var all = [];
    if(api.getScroll() > 0) {
        var self = $('a[name="' + api.getScroll() + '"]').parent().find('div.gif.media, div.gif-link.media');
        var after = $('a[name="' + api.getScroll() + '"]').parent().nextAll().find('div.gif.media, div.gif-link.media');
        all = $.merge(self, after);
    } else {
        all = $('div.gif.media, div.gif-link.media');
    }
    all.each(function() {
        replaceImage($(this));
    });
}

// load all videos
function loadAllVideos() {
    var all = [];
    if(api.getScroll() > 0) {
        var self = $('a[name="' + api.getScroll() + '"]').parent().find('div.video.media');
        var after = $('a[name="' + api.getScroll() + '"]').parent().nextAll().find('div.video.media');
        all = $.merge(self, after);
    } else {
        all = $('div.video.media');
    }
    all.each(function() {
        replaceVideo($(this));
    });
}

function getURLParameter(a, name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(a.search)||[,null])[1]
    );
}


function changeConfiguration(landscape) {

    if(api.isBenderEnabled() && api.getBenderPosition() == 3) {
        if(landscape) {
            $(".header .bender").addClass("hidden");
            $(".body .bender").removeClass("hidden");
        } else {
            $(".header .bender").removeClass("hidden");
            $(".body .bender").addClass("hidden");
        }
    }

    if(api.getShowMenu() == 3) {
        if(landscape) {
            $(".header .post-menu-button").hide();
            $(".header .post-buttons button.in-menu").show();
            console.log(api.getShowMenu() + "lel");
        } else {
            $(".header .post-menu-button").show();
            $(".header .post-buttons button.in-menu").hide();
            console.log(api.getShowMenu());
        }
    }
}
