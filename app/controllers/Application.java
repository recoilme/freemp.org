package controllers;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import sun.security.provider.MD5;

public class Application extends Controller {

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
        OrientGraph graph = DbWrapper.dbFactory.getTx();
        Iterable<Vertex> results = null;
        try {
            results = graph.command(new OCommandSQL("select * , first(in('author').username) as uname from Article order by modified desc")).execute();

            for (Vertex post:results){
                System.out.println("posts:"+post.toString()+post.getProperty("content"));
            }
        }
        catch (Exception e) {
            System.out.println("Ex getQueryResult:"+e.toString());
        }
        finally {
            graph.shutdown();
        }
        render();
    }

    public static void register() {

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
    /*

    @Before
    static void addDb() {
        OrientGraph graph = dbFactory.getTx();
        try {
            if (false) {
                for (Vertex v : graph.getVertices()) {
                    graph.removeVertex(v);
                }
                graph.commit();
            }

            graph.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", "Usr"));
            graph.commit();

            OrientVertex u = graph.addVertex("class:Usr", "name", "admin", "email", "", "pwd", "admin");
            OrientVertex p = graph.addVertex( "class:Post", "title", "title", "body", "body");
            p.addEdge("class:Author",u,null,null,"date","2013-07-30");
            graph.commit();
        }
        catch (Exception e) {
            System.out.println("Error:|"+e.toString()+"|");
            graph.rollback();
        }
        finally {
            graph.shutdown();
        }
    }
    */
}