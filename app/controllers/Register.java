package controllers;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import models.ClsUser;
import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoilme on 19/03/14.
 */
public class Register extends Controller {
    @Before
    static void setConnectedUser() {
        Application.setConnectedUser();
    }

    public static void index() {
        render();
    }

    public static void createNewDb() {
        //System.out.println(renderArgs.get("username"));
        if (renderArgs.get("username")!=null && renderArgs.get("username").equals("admin")) {
            OrientGraph graph = DbWrapper.graph;
            for (Edge e : graph.getEdges()) {
                System.out.println(e.toString());
                e.remove();
            }
            for (Vertex v : graph.getVertices()) {
                System.out.println(v.toString());
                graph.removeVertex(v);
            }
        }

    }

    public static void save(String username, String email, String password) {
        if (username.contains("@")) {
            flash.error("Error: username must not contain:'@'");
            index();
        }
        if (username.length()<1) {
            flash.error("Error: username length must be more then 0");
            index();
        }
        if (email.length()<=2) {
            flash.error("Error: email length must be more then 2");
            index();
        }
        if (password.length()<5) {
            flash.error("Error: password length must be more then 5");
            index();
        }
        if (DbWrapper.getVertex("ClsUser.email", email)!=null) {
            flash.error("Error: user with this email:'"+ email + "' allready exists in database");
            index();
        }
        if (DbWrapper.getVertex("ClsUser.username", username)!=null) {
            flash.error("Error: user with this username:'"+ username + "' allready exists in database");
            index();
        }
        ClsUser user = new ClsUser();
        user.username = username;
        user.email=email;
        user.password=password;
        user.remember=true;
        Vertex vUser = DbWrapper.saveClass(user);
        if (vUser!=null) {

            session.clear();
            response.removeCookie("rememberme");
            session.put("username", vUser.getId());

            Date expiration = new Date();
            String duration = Play.configuration.getProperty("secure.rememberme.duration","30d");
            expiration.setTime(expiration.getTime() + ((long) Time.parseDuration(duration)) * 1000L );
            response.setCookie("rememberme", Crypto.sign(vUser.getId() + "-" + expiration.getTime()) + "-" + vUser.getId() + "-" + expiration.getTime(),null,"/",
                    Time.parseDuration(duration),false,true);
            try {
                Secure.login();
                Application.index();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        else {
            flash.error("Error: db commit failed");
            index();
        }
    }
}
