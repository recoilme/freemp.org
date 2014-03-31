package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jndi.toolkit.url.Uri;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import play.libs.WS;
import play.mvc.Controller;

/**
 * Created by recoil on 29.03.14.
 */
public class Search extends Controller {

    public static void index(){
        //http://www.last.fm/music/The+Prodigy/+wiki
        String url = String.format("http://www.lastfm.ru/music/%s/+wiki",
                "The+Prodigy");
        Document doc = Jsoup.parse(WS.url(url).get().getString());

        String wiki = doc.getElementById("wiki").text();
        renderHtml(wiki);
    }

}
