package controllers;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import models.ClsArticle;
import models.ClsPost;
import play.mvc.*;

import java.util.*;

public class Application extends Controller {

    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            Vertex v = DbWrapper.getVertexById(Security.connected());
            if (v != null) {
                renderArgs.put("username", v.getProperty("username"));
            }
        }
        else {
            //System.out.println("Anon");
        }
    }

    public static void index() {
        List<ClsArticle> articles = new ArrayList<ClsArticle>();

        OrientGraph graph = DbWrapper.graph;
        Iterable<Vertex> results = null;

        try {
            results = graph.command(
                    new OCommandSQL("select *, in('author')[0].username as uname from ClsPost order by modified desc")//"traverse in_author from (select * from Article)")
                    //"select from Article where any()")
                    //new OCommandSQL("select * , first(in('author').username) as uname from Article order by modified desc limit 10")
            ).execute();

            for (Vertex post:results){

                Iterable<Edge> e = post.getEdges(Direction.OUT, "comment");
                int size = 0;
                for(Edge value : e) {
                    size++;
                }
                //System.out.println("size"+size );

                ClsArticle article = new ClsArticle();
                article.content = post.getProperty("content");
                article.created = post.getProperty("created");
                article.uname = post.getProperty("uname");
                article.id = ""+(ORID)post.getId();
                article.commentsCount = size;
                System.out.println("111!"+DbWrapper.Vertex2String(post));
                articles.add(article);
            }
        }
        catch (Exception e) {
            System.out.println("Ex getQueryResult:"+e.toString());
        }
        render(articles);
    }


    /*
            graph.addVertex( "class:User", "email", email, "password", password, "remember", remember);
            graph.commit();

            //1st sposob
            OrientGraphQuery oQuery = (OrientGraphQuery) graph.query();
            Iterable<Vertex> results1 = oQuery.labels("User").has("email", email).vertices();
            for (Vertex v: results1) {
                System.out.println("Vertex1:"+v.toString()+v.getProperty("password"));
            }
            //2 sposob
            for( Vertex v : graph.getVertices("User.email", email) ) {
                System.out.println("Vertex2: " + v );
            }
            //3 sposob
            Iterable<Vertex> results2 = graph.command(new OCommandSQL("select from User where email = '" + email + "'")).execute();
            for (Vertex v: results2) {
                System.out.println("Vertex3:"+v.toString()+v.getProperty("password"));
            }
            //gremlin?
            */
}