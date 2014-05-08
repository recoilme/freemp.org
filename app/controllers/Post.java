package controllers;


import com.google.common.io.Files;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import models.ClsArticle;
import models.ClsPost;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import play.Logger;
import play.Play;
import play.i18n.Lang;
import play.libs.Images;
import play.mvc.Before;
import play.mvc.Controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by recoilme on 24/03/14.
 */
public class Post extends Controller {
    @Before
    static void setConnectedUser() {
        Application.setConnectedUser();

    }

    public static void newpost() {
        render();
    }

    public static void id(String id) {
        try {
            Vertex vPost = DbWrapper.getVertexById(id);
            Iterable<Edge> authors = vPost.getEdges(Direction.IN, "author");
            Edge edge = authors.iterator().next();
            Vertex vAuthor = edge.getVertex(Direction.OUT);
            if (vAuthor == null || vPost == null) {
                error(404,"");
            }
            else {
                List<ClsArticle> articles = new ArrayList<ClsArticle>();
                OrientGraph graph = DbWrapper.graph;
                Iterable<Vertex> results = null;
                results = graph.command(
                        new OCommandSQL("select content,created, in('author')[0].username as uname from (traverse out_comment from "+id+") where in_comment is not null order by created asc")
                        //new OCommandSQL("select content,created, in('author')[0].username as uname from (traverse out_comment from "+id+") where @class = 'ClsPost' order by created asc")
                    ).execute();

                for (Vertex comments:results){
                    //System.out.println(DbWrapper.Vertex2String(comments));
                    ClsArticle article = new ClsArticle();
                    article.content = comments.getProperty("content");
                    article.created = comments.getProperty("created");
                    article.uname = comments.getProperty("uname");
                    article.id = id;
                    //System.out.println("111!"+DbWrapper.Vertex2String(post));
                    articles.add(article);
                }
                String postId = id;
                render(vPost,vAuthor,postId,articles);
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
            error(404,"");
        }
    }

    public static void addcomment(String content, String postid) {

        if(Security.isConnected()) {
            Vertex user = DbWrapper.getVertexById(Security.connected());
            if (user != null) {
                Vertex vComment = newArticle(content);
                Vertex vPost = DbWrapper.getVertexById(postid);

                if (vComment != null && vPost != null) {
                    //udpate modified time
                    vPost.setProperty("modified",System.currentTimeMillis());
                    Edge comment = DbWrapper.addEdge("comment", (ORID) vPost.getId(), (ORID) vComment.getId());
                    Edge author = DbWrapper.addEdge("author", (ORID) user.getId(), (ORID) vComment.getId());

                    if (comment != null) {
                        id(postid);
                    }
                }
            }
        }
    }

    public static void save(String content) {
        if(Security.isConnected()) {
            Vertex user = DbWrapper.getVertexById(Security.connected());
            if (user != null) {
                Vertex vPost = newArticle(content);
                if (vPost != null) {
                    Edge author = DbWrapper.addEdge("author", (ORID) user.getId(), (ORID) vPost.getId());
                    if (author != null) {
                        Application.index();
                    }
                }
            }
        }
    }

    public static Vertex newArticle(String content) {
        content = ("" + content).trim();
        if (content.isEmpty() || content.equals("<p><br></p>")) return null;
        System.out.println("'"+content+"'");
        content = Policy.POLICY_DEFINITION.sanitize(content);
        System.out.println("'"+content+"'");
        ClsPost clsPost = new ClsPost();
        clsPost.content = content;
        long now = System.currentTimeMillis();
        clsPost.modified = now;
        clsPost.created = now;
        clsPost.lang = Lang.get();
        return DbWrapper.saveClass(clsPost);
    }

    public static void saveimage() {
        String result = "";
        try {
            File[] images = params.get("file", File[].class);
            SimpleDateFormat yyMM = new SimpleDateFormat("yyyyMM");
            String catalog = yyMM.format(new Date()).substring(3);
            SimpleDateFormat nameFmt = new SimpleDateFormat("ddHHmmssS");
            for (File f : images) {
                Images.resize(f,f,800,-1);

                FileInputStream is = new FileInputStream(f);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                String original = "img" + File.separator + catalog+ File.separator + Long.toHexString(Long.parseLong(nameFmt.format(new Date())))+ "."+FilenameUtils.getExtension(f.getAbsolutePath().toString()).toLowerCase();
                if (new File(original).getParentFile().mkdirs()) {
                   //TODO create noindex
                }
                IOUtils.copy(is, new FileOutputStream(Play.getFile(original)));
                //renderText(original);
                Logger.info(File.separator+original.replace("img","i"));
                renderText("http://freemp.org"+File.separator+original.replace("img","i"));
            }
        }
        catch (Exception e) {
            renderText("Error:"+e.toString());
        }
        /*
        File[] images = params.get("file", File[].class);
        Logger.info("Absolute on where to send %s", Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator);

        for (File f : images) {
            Logger.info(f.getName());
            Logger.info(f.getAbsolutePath().toString());
            try {
                FileInputStream f = new FileInputStream(f.);
                InputStream data = request.body;
                Logger.info("dat:"+data.available());
                FileOutputStream moveTo = new FileOutputStream(new File(Play.getFile("").getAbsolutePath())
                        + File.separator + "uploads" + File.separator + f.getName());

                IOUtils.copy(data, moveTo);
                //Files.move(f,new File(Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator+f.getName()));

            } catch (Exception ex) {

                // catch file exception
                // catch IO Exception later on
                renderText("{success: false}" + ex.toString());
                Logger.info(ex.toString());
                System.out.println("Exception is:" + ex);
            }
        }
        renderText("http://i.imgur.com/uDUhoQh.png");
        */
    }
}
