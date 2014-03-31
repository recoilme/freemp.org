package controllers;

import com.google.gson.*;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
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

    public static final String special = "'\\+-&|!(){}[]^\"~*?:;,";

    public static void index() {
        renderHtml("Ok");
    }

    public static void name(String name){

        name = (""+name).toLowerCase().replace(special,"").replace(" ","_").trim();
        Vertex vArtist = DbWrapper.getVertex("ClsArtist.searchName", name);
        if (vArtist == null) {
            JsonElement artistLastfm = searchArtistLastfm(name);
            if (artistLastfm == null) {
                renderText("artist not found on Last.fm");
            }
            vArtist = parseArtist(artistLastfm);
        }
        if (vArtist == null) {
            renderText("vArtist is null");
        }
        //getArtistBio(vArtist);
        Iterable<Edge> similars = vArtist.getEdges(Direction.OUT, "similarNameArtist");
        for (Edge edge:similars) {
            System.out.println(edge.getVertex(Direction.IN).getProperty("artistName"));
        }
        render(vArtist,similars);
        /*
        String url = String.format("http://www.lastfm.ru/music/%s/+wiki",
                name.replace(" ","+"));
        Document doc = Jsoup.parse(WS.url(url).get().getString());

        String wiki = doc.getElementById("wiki").text();
        renderHtml(wiki);
        */
    }

    public static void getArtistBio(Vertex vArtist){

    }

    public static Vertex parseArtist(JsonElement jsonElement) {
        ClsArtist   tmpArtist   = null;
        Vertex      mainArtist  = null;
        Vertex      slaveArtist = null;
        try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject = jsonObject.getAsJsonObject("results");
            jsonObject = jsonObject.getAsJsonObject("artistmatches");
            JsonArray artists = jsonObject.getAsJsonArray("artist");

            for (int i=0;i<artists.size();i++) {
                jsonObject = artists.get(i).getAsJsonObject();
                String mbid = ""+jsonObject.get("mbid").getAsString();
                if (!mbid.equals("")) {

                    String searchName = (""+jsonObject.get("name").getAsString()).toLowerCase().replace(special,"").replace(" ","_").trim();
                    tmpArtist = new ClsArtist();
                    tmpArtist.searchName = searchName;
                    tmpArtist.artistName = ""+jsonObject.get("name").getAsString();
                    tmpArtist.mbid = ""+jsonObject.get("mbid").getAsString();
                    tmpArtist.lastfmUrl = ""+jsonObject.get("url").getAsString();
                    tmpArtist.images = jsonObject.getAsJsonArray("image").toString();

                    if (i==0) {
                        mainArtist = DbWrapper.saveClass(tmpArtist);
                    }
                    else {
                        slaveArtist = DbWrapper.saveClass(tmpArtist);
                        if (mainArtist!=null && slaveArtist!=null) {
                            Edge similarNameArtist = DbWrapper.addEdge("similarNameArtist", (ORID) mainArtist.getId(), (ORID) slaveArtist.getId());
                        }
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return mainArtist;
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
                String artistSearch = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.search&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&format=json&limit=3", artist);
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
