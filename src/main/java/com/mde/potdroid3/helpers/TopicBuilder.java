package com.mde.potdroid3.helpers;

import android.content.Context;
import com.mde.potdroid3.models.Post;
import com.mde.potdroid3.models.Topic;
import com.samskivert.mustache.Mustache;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by oli on 8/10/13.
 */
public class TopicBuilder {

    private Context mContext;
    private HashMap<String, String> mSmileys = new HashMap<String, String>();
    private SettingsWrapper mSettings;
    private BBCodeParser mParser;

    public TopicBuilder(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);
        mParser = getBBCodeParserInstance();

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

    public String parse(Topic t) throws IOException {
        InputStream is = mContext.getResources().getAssets().open("thread.html");
        Reader reader = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        Mustache.compiler().compile(reader).execute(new TopicContext(t), sw);
        return sw.toString();
    }

    private String parseSmileys(String code) {
        Iterator<Map.Entry<String, String>> i = mSmileys.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry<String, String> me = i.next();
            code = code.replace(me.getKey(),
                    "<img src=\"smileys/" + me.getValue() + "\" alt=\"+ " + me.getKey() +"\" />");
        }
        return code;
    }

    class TopicContext {
        private Topic mTopic;

        public TopicContext(Topic t) {
            mTopic = t;
        }

        public List<PostContext> getPosts() {
            List<PostContext> pc = new ArrayList<PostContext>();
            for(Post p: mTopic.getPosts())
                pc.add(new PostContext(p));
            return pc;
        }
    }

    class PostContext {
        private Post mPost;

        public PostContext(Post p) {
            mPost = p;
        }

        public Integer getId() {
            return mPost.getId();
        }

        public String getAuthor() {
            return mPost.getAuthor().getNick();
        }

        public Integer getAuthorId() {
            return mPost.getAuthor().getId();
        }

        public String getAvatar() {
            return mPost.getAuthor().getAvatarFile();
        }

        public Integer getAvatarId() {
            return mPost.getAuthor().getAvatarId();
        }

        public String getAvatarPath() {
            return mPost.getAuthor().getAvatarLocalFileUrl(mContext);
        }

        public String getDate() {
            return mPost.getDate().toString();
        }

        public String getTitle() {
            return mPost.getTitle();
        }

        public String getText() {
            String text = "parse error";
            try {
                text = mParser.parse(mPost.getText());
                text = parseSmileys(text);
            } catch (BBCodeParser.UnknownErrorException e) {
                text = "Could not parse post!";
            }
            return text;
        }
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
