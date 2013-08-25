package com.mde.potdroid3.helpers;

import android.content.Context;
import com.mde.potdroid3.models.Post;
import com.mde.potdroid3.models.Topic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by oli on 8/10/13.
 */
public class TopicBuilder {

    private Context mContext;
    private HashMap<String, String> mSmileys = new HashMap<String, String>();
    private SettingsWrapper mSettings;

    public TopicBuilder(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);

        // smileys
        mSmileys.put(":bang:", "banghead.gif");
        mSmileys.put(":D", "biggrin.gif");
        mSmileys.put(":confused:", "confused.gif");
        mSmileys.put(":huch:", "freaked.gif");
        mSmileys.put(":hm:", "hm.gif");
        mSmileys.put(":mata:", "mata.gif");
        mSmileys.put(":what:", "sceptic.gif");
        mSmileys.put(":moo:", "smiley-pillepalle.gif");
        mSmileys.put(":wurgs:", "urgs.gif");
        mSmileys.put(";)", "wink.gif");
        mSmileys.put(":zyklop:", "icon1.gif");
        mSmileys.put(":P", "icon2.gif");
        mSmileys.put("^^", "icon5.gif");
        mSmileys.put(":)", "icon7.gif");
        mSmileys.put(":|", "icon8.gif");
        mSmileys.put(":(", "icon12.gif");
        mSmileys.put(":mad:", "icon13.gif");
        mSmileys.put(":eek:", "icon15.gif");
        mSmileys.put(":o", "icon16.gif");
        mSmileys.put(":roll:", "icon18.gif");
    }

    public String parse(Topic t) {
        StringBuilder sb = new StringBuilder();
        BBCodeParser parser = getBBCodeParserInstance();

        // document header
        sb.append("<!DOCTYPE html>\n<html><head><title>-</title>\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n");
        sb.append("<script type=\"text/javascript\" src=\"jquery.js\"></script>\n");
        sb.append("<script type=\"text/javascript\" src=\"thread.js\"></script>\n");
        sb.append("<link rel=\"stylesheet\" href=\"thread.css\" type=\"text/css\" />\n");
        sb.append("</head><body>\n");

        String text;

        for(Post p: t.getPosts()) {
            sb.append("<section ");

            sb.append("data-id=\"").append(p.getId()).append("\" ");
            sb.append("data-user=\"").append(p.getAuthor().getNick()).append("\" ");
            sb.append("data-user-id=\"").append(p.getAuthor().getId()).append("\" ");
            sb.append("data-user-avatar=\"").append(p.getAuthor().getAvatarFile()).append("\" ");
            sb.append("data-user-avatar-id=\"").append(p.getAuthor().getAvatarId()).append("\" ");
            sb.append("data-user-avatar-path=\"").append(p.getAuthor().getAvatarLocalFileUrl(mContext)).append("\" ");
            sb.append(">\n");

            // header section
            sb.append("<header>\n");
            sb.append("<div class=\"icons\"><a href=\"#\" class=\"reply\"></a></div>\n");
            sb.append("<div class=\"icons\"><a href=\"#\" class=\"edit\"></a></div>\n");
            sb.append("<div class=\"author\" ");
            sb.append("data-path=\"").append(p.getAuthor().getAvatarLocalFileUrl(mContext)).append("\">");

            if(mSettings.showBenders())
                sb.append("<div class=\"bender\"></div>");

            sb.append(p.getAuthor().getNick()).append("</div>\n");
            sb.append("<div class=\"date\">").append(p.getDate()).append("</div>\n");
            if(!p.getTitle().isEmpty())
                sb.append("<div class=\"title\">").append(p.getTitle()).append("</div>\n");
            sb.append("</header>\n");

            try {
                text = parser.parse(p.getText());
                text = parseSmileys(text);
            } catch (BBCodeParser.UnknownErrorException e) {
                text = "Could not parse post!";
            }
            sb.append("<article><div>").append(text).append("</div></article>\n");

            sb.append("</section>\n");
        }

        sb.append("</body></html>");

        return sb.toString();
    }

    private String parseSmileys(String code) {
        Iterator<Map.Entry<String, String>> i = mSmileys.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry<String, String> me = i.next();
            code = code.replace(me.getKey(), "<img src=\"smileys/" + me.getValue() + "\" alt=\"+ " +
                    me.getKey() +"\" />");
        }
        return code;
    }

    private static BBCodeParser getBBCodeParserInstance() {
        BBCodeParser a = new BBCodeParser();


        String allNodes = "string, b, u, s, i, mod, spoiler, " +
                "code, img, quote, url, list, table, m";
        String inLinks = "string, b, u, s, i, mod, img, url, list, table, m";

        class SimpleTag extends BBCodeParser.BBCodeTag {
            public SimpleTag(String tag, String name) {
                super(tag, name);
                this.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_REOPEN);
                this.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
            }

            public SimpleTag(String tag, String name, String allowed) {
                super(tag, name, allowed);
                this.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_REOPEN);
                this.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
            }

            public String html(String content, List<String> args) {
                return "";
            }
        }

        a.registerTag(new SimpleTag("b", "bold") {
            @Override
            public String html(String content, List<String> args) {
                return "<strong>" + content + "</strong>";
            }
        });

        a.registerTag(new SimpleTag("m", "monotype", "string") {
            @Override
            public String html(String content, List<String> args) {
                return "<pre class=\"inline\">" + content + "</pre>";
            }
        });

        a.registerTag(new SimpleTag("u", "underline") {
            @Override
            public String html(String content, List<String> args) {
                return "<u>" + content + "</u>";
            }
        });

        a.registerTag(new SimpleTag("s", "strike") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"strike\">" + content + "</span>";
            }
        });

        a.registerTag(new SimpleTag("i", "italic") {
            @Override
            public String html(String content, List<String> args) {
                return "<em>" + content + "</em>";
            }
        });

        a.registerTag(new SimpleTag("code", "code", "string") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"code\">" + content + "</span>";
            }
        });

        a.registerTag(new SimpleTag("spoiler", "spoiler") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"spoiler\">" + content + "</span>";
            }
        });

        a.registerTag(new SimpleTag("mod", "mod") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"mod\">" + content + "</span>";
            }
        });

        a.registerTag(new SimpleTag("trigger", "trigger") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"trigger\">" + content + "</span>";
            }
        });

        BBCodeParser.BBCodeTag list = new BBCodeParser.BBCodeTag("list", "list", "*") {
            @Override
            public String html(String content, List<String> args) {
                return "<span class=\"trigger\">" + content + "</span>";
            }
        };
        list.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        list.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        list.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        list.setInvalidRecoveryTag("*");
        a.registerTag(list);

        BBCodeParser.BBCodeTag item = new BBCodeParser.BBCodeTag("*", "listitem") {
            @Override
            public String html(String content, List<String> args) {
                return "<li>" + content + "</li>";
            }
        };
        item.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        item.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        a.registerTag(item);

        a.registerTag(new BBCodeParser.BBCodeTag("url", "link", inLinks) {
            @Override
            public String html(String content, List<String> args) {
                String description = content;
                if(args.size() > 0)
                    description = args.get(0);
                return "<a href=\"" + description + "\">" + content + "</a>";
            }
        });

        a.registerTag(new SimpleTag("quote", "quote") {
            @Override
            public String html(String content, List<String> args) {
                if(args.size() == 3)
                    return "<blockquote><div class=\"author\">" + args.get(2) + "</div>"
                            +"<div class=\"content\">" + content + "</div></blockquote>";
                else
                    return "<blockquote><div class=\"content\">" + content + "</div></blockquote>";
            }
        });

        a.registerTag(new SimpleTag("img", "image", "string") {
            @Override
            public String html(String content, List<String> args) {
                return "<img src=\"" + content + "\" class=\"\" alt=\"" + content + "\" />";
            }
        });

        a.registerTag(new SimpleTag("tex", "latex", "string") {
            @Override
            public String html(String content, List<String> args) {
                String code = content;
                try {
                    code = URLEncoder.encode(code, "utf-8");
                    code = code.replace("<br />","\n");
                    return "<img src=\"http://chart.apis.google.com/chart?chco=000000&chf=bg,s,ffffffff&cht=tx&chl=" + code + "\" class=\"tex\" />";
                } catch (UnsupportedEncodingException e) {
                    return "";
                }

            }
        });

        BBCodeParser.BBCodeTag table = new BBCodeParser.BBCodeTag("table", "table", "--") {
            @Override
            public String html(String content, List<String> args) {
                return "<table>" + content + "</table>";
            }
        };
        table.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        a.registerTag(table);

        BBCodeParser.BBCodeTag row = new BBCodeParser.BBCodeTag("--", "tablerow", "||") {
            @Override
            public String html(String content, List<String> args) {
                return "<tr>" + content + "</tr>";
            }
        };
        table.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        a.registerTag(row);

        BBCodeParser.BBCodeTag col = new BBCodeParser.BBCodeTag("||", "tablecol") {
            @Override
            public String html(String content, List<String> args) {
                return "<td>" + content + "</td>";
            }
        };
        table.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        table.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        a.registerTag(col);
        return a;
    }

}
