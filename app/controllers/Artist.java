package controllers;

import com.google.gson.*;
import com.tinkerpop.blueprints.Vertex;
import models.ClsArtist;
import models.DbWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.libs.WS;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoil on 30.03.14.
 */
public class Artist extends Controller {

    public static void index() {
        renderHtml("Ok");
    }

    public static void name(String name){
        name = (""+name).toLowerCase().replace(";","").replace("'","").replace("\"","").trim();
        JsonElement artistLastfm= searchArtistLastfm(name);
        if (artistLastfm!=null) {
            ClsArtist artist = parseArtist(artistLastfm);
            renderText(artist.artistName);
        }
        else {
            renderText("ClsArtist not found");
        }
        /*
        String url = String.format("http://www.lastfm.ru/music/%s/+wiki",
                name.replace(" ","+"));
        Document doc = Jsoup.parse(WS.url(url).get().getString());

        String wiki = doc.getElementById("wiki").text();
        renderHtml(wiki);
        */
    }

    public static ClsArtist parseArtist(JsonElement jsonElement) {
        ClsArtist artist = null;
        try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject = jsonObject.getAsJsonObject("results");
            jsonObject = jsonObject.getAsJsonObject("artistmatches");
            JsonArray artists = jsonObject.getAsJsonArray("artist");
            for (int i=0;i<artists.size();i++) {
                jsonObject = artists.get(i).getAsJsonObject();
                String mbid = ""+jsonObject.get("mbid").getAsString();
                if (!mbid.equals("")) {
                    if (i==0) {
                        artist = new ClsArtist();
                        artist.artistName = ""+jsonObject.get("name").getAsString();
                        artist.mbid = ""+jsonObject.get("mbid").getAsString();
                        artist.lastfmUrl = ""+jsonObject.get("url").getAsString();
                        artist.images = jsonObject.getAsJsonArray("image").toString();
                        DbWrapper.saveClass(artist);
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return artist;
    }

    //search artist in web and add 2 db or get from db if exists
    public static JsonElement searchArtistLastfm(String artist) {
        JsonElement jsonElement = null;
        try {
            Vertex searchArtist = DbWrapper.getVertex("SearchArtist.artist",artist);
            if (searchArtist!=null) {
                JsonParser jsonParser = new JsonParser();
                jsonElement = (JsonElement)jsonParser.parse((String)searchArtist.getProperty("content"));
                System.out.print("fromdb");
            }
            else {
                String artistSearch = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.search&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&format=json&limit=5", artist);
                jsonElement = WS.url(artistSearch).get().getJson();
                Map<String, Object> props = new HashMap<String, Object>();
                props.put("artist", artist);
                props.put("content", jsonElement.toString());
                props.put("modified", System.currentTimeMillis());
                searchArtist = DbWrapper.addVertex("SearchArtist", props);
                System.out.print("fromweb");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonElement;
    }
}
