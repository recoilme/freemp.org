package controllers;

import com.google.gson.*;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import models.ClsArtist;
import models.ClsBio;
import models.DbWrapper;
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
        if(Security.isConnected()) {
            Vertex v = DbWrapper.getVertex(Security.connected().contains("@")?"User.email":"User.username", Security.connected());
            if (v != null) {
                renderArgs.put("username", v.getProperty("username"));
            }
        }
    }

    public static void index() {
        renderHtml("Ok");
    }

    public static void mbid(String mbid) {
        OrientGraph graph = DbWrapper.dbFactory.getTx();
        Vertex vArtist = DbWrapper.getVertex("ClsArtist.mbid", mbid);
        if (vArtist == null) {
            renderText("vArtist is null");
        }

        List<Vertex> similarNames = new ArrayList<Vertex>();
        try {
            Iterable<Edge> similars = vArtist.getEdges(Direction.OUT, "similarNameArtist");
            for (Edge edge : similars) {
                //System.out.println(edge.toString());
                similarNames.add(edge.getVertex(Direction.IN));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(DbWrapper.Vertex2String(vArtist));
        Vertex vBio = getArtistBio(vArtist);
        //System.out.println(DbWrapper.Vertex2String(vBio));
        render(vArtist,similarNames,vBio);

    }

    public static void search(String searchtxt) {
        searchtxt = (""+searchtxt).toLowerCase().replace(Artist.special,"").replace(" ","_").trim();
        Vertex vArtist = DbWrapper.getVertex("ClsArtist.searchName", searchtxt);
        if (vArtist == null) {
            JsonElement artistLastfm = Artist.searchArtistLastfm(searchtxt);
            if (artistLastfm == null) {
                renderText("artist not found on Last.fm");
            }
            vArtist = Artist.parseArtist(artistLastfm);
        }
        if (vArtist == null) {
            renderText("vArtist is null");
        }
        Artist.mbid(vArtist.getProperty("mbid").toString());
    }

    public static Vertex getArtistBio(Vertex vArtist){
        String lang = Lang.get();
        System.out.println("3456");
            Iterable<Edge> bios = vArtist.getEdges(Direction.OUT, "artistBio");


            for (Edge edge : bios) {

                System.out.println("has123"+edge.toString() );

                Vertex v = edge.getVertex(Direction.IN);
                System.out.println(DbWrapper.Vertex2String(v));
                if (v.getProperty("locale").equals(lang)) {
                    System.out.println("biodb");
                    return v;
                }

            }

        return null;
        /*
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
            System.out.println("bioweb");
            return vBio;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        */
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
                System.out.print("fromdb");
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
                System.out.print("fromweb");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonElement;
    }
}
