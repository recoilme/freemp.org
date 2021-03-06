package controllers;

import com.google.gson.*;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import models.ClsArtist;
import models.ClsBio;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by recoil on 30.03.14.
 */
public class Artist extends Controller {

    public static final String special = "'\\+-&|!(){}[]^\"~*?:;,";

    @Before
    static void setConnectedUser() {
        Application.setConnectedUser();
    }

    public static void index() {
        renderHtml("Ok");
    }

    public static void albums(String mbid) {

    }

    public static void mbid(String mbid) {
        Vertex vArtist = DbWrapper.getVertex("ClsArtist.mbid", mbid);
        if (vArtist == null) {
            error("Artist not found");
        }

        List<Vertex> similarNames = new ArrayList<Vertex>();
        String img = "";
        try {
            Iterable<Edge> similars = vArtist.getEdges(Direction.BOTH, "similarNameArtist");
            String vId = vArtist.getId().toString();
            for (Edge edge : similars) {
                //System.out.println(edge.toString());
                if (vId.equals(""+edge.getVertex(Direction.OUT).getId())) {
                    similarNames.add(edge.getVertex(Direction.IN));
                }
                else {
                    similarNames.add(edge.getVertex(Direction.OUT));
                }
            }
            JsonParser jsonParser = new JsonParser();
            JsonArray imgs = jsonParser.parse((String) vArtist.getProperty("images")).getAsJsonArray();
            for (int i=0;i<imgs.size();i++) {
                String s=""+imgs.get(i).getAsJsonObject().get("#text").getAsString();
                if (!s.equals("")) {
                    img = s;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(DbWrapper.Vertex2String(vArtist));
        Vertex vBio = getArtistBio(vArtist);
        //System.out.println(DbWrapper.Vertex2String(vBio));
        render(vArtist,similarNames,vBio,img);

    }

    public static void s(String q,String l) {
        if (l!=null && (l.equals("en") || l.equals("ru"))) {
            Lang.change(l);
        }
        search(q);
    }

    public static void search(String q) {
        String searchtxt = (""+q).toLowerCase().replace(Artist.special,"").replace(" ","_").trim();
        Vertex vArtist = DbWrapper.getVertex("ClsArtist.searchName", searchtxt);
        DbWrapper.Vertex2String(vArtist);
        if (vArtist == null) {
            JsonElement artistLastfm = Artist.searchArtistLastfm(searchtxt);
            if (artistLastfm == null) {
                renderText("artist not found on Last.fm");
            }
            vArtist = Artist.parseArtist(artistLastfm);
        }
        else {
            System.out.println("fromdb");
        }
        if (vArtist == null) {
            error("Artist: "+q+" - not found");
        }
        Artist.mbid(vArtist.getProperty("mbid").toString());
    }

    public static Vertex getArtistBio(Vertex vArtist){
        String lang = Lang.get();
        System.out.println("lang:"+lang);
        Iterable<Edge> bios = vArtist.getEdges(Direction.OUT, "artistBio");


        for (Edge edge : bios) {

            Vertex v = edge.getVertex(Direction.IN);
            if (v.getProperty("locale").equals(lang)) {
                return v;
            }

        }

        try {

            //Vertex not found^ create it!
            ClsBio bio = new ClsBio();

            String url = String.format("http://www."+ Messages.get("lastfm")+"/music/%s/+wiki",
                    vArtist.getProperty("lastfmUrl"));
            Document doc = Jsoup.parse(WS.url(url).get().getString());

            String wiki = doc.getElementById("wiki").text();
            String lastEdit = doc.getElementsByClass("lastEdit").html();
            lastEdit = (""+lastEdit).replace("/user/","http://last.fm/user/");
            bio.bio = wiki;
            bio.lastEdit = lastEdit;
            bio.locale = lang;
            Vertex vBio = DbWrapper.saveClass(bio);
            Edge artistBio = DbWrapper.addEdge("artistBio",(ORID)vArtist.getId(),(ORID)vBio.getId());
            //"".escapeHtml();

            return vBio;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }

    }

    public static Vertex parseArtist(JsonElement jsonElement) {
        ClsArtist   tmpArtist   = null;
        Vertex      mainArtist  = null;
        Vertex      slaveArtist = null;
        System.out.println("fromweb");
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
                    String[] split = jsonObject.get("url").getAsString().split("/");
                    if (split!=null && split.length>0) {
                        tmpArtist.lastfmUrl = split[split.length-1];
                    }
                    else {
                        tmpArtist.lastfmUrl = "";
                    }
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

            }
            else {
                String artistSearch = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.search&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&format=json&limit=3", artist);

                jsonElement = WS.url(artistSearch).get().getJson();
                //test json
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Map<String, Object> props = new HashMap<String, Object>();
                props.put("artist", artist);
                props.put("content", jsonElement.toString());
                props.put("modified", System.currentTimeMillis());
                searchArtist = DbWrapper.addVertex("SearchArtist", props);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonElement;
    }
}
