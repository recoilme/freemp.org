package controllers;


import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import models.ClsComment;
import models.ClsPost;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

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
                OrientGraph graph = DbWrapper.graph;
                Iterable<Vertex> results = null;
                results = graph.command(
                        new OCommandSQL("select content,created, in('author')[0].username as uname from (traverse out_comment from "+id+") order by created desc")
                    ).execute();

                for (Vertex comments:results){
                    System.out.println(DbWrapper.Vertex2String(comments));
                }
                String postId = id;
                render(vPost,vAuthor,postId);
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
                PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES).and(Sanitizers.BLOCKS);
                content = policy.sanitize(content);
                ClsComment clsComment = new ClsComment();
                clsComment.content = content;
                long now = System.currentTimeMillis();
                clsComment.modified = now;
                clsComment.created = now;
                Vertex vComment = DbWrapper.saveClass(clsComment);
                if (vComment != null) {
                    Edge comment = DbWrapper.addEdge("comment", (ORID) DbWrapper.getVertexById(postid).getId(), (ORID) vComment.getId());
                    Edge author  = DbWrapper.addEdge("author", (ORID) user.getId(), (ORID) vComment.getId());
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
                PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES).and(Sanitizers.BLOCKS);
                content = policy.sanitize(content);
                ClsPost clsPost = new ClsPost();
                clsPost.content = content;
                long now = System.currentTimeMillis();
                clsPost.modified = now;
                clsPost.created = now;
                Vertex vPost = DbWrapper.saveClass(clsPost);
                if (vPost != null) {
                    Edge author = DbWrapper.addEdge("author", (ORID) user.getId(), (ORID) vPost.getId());
                    if (author != null) {
                        Application.index();
                    }
                }
            }
        }
    }
}
