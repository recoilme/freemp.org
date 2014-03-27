package controllers;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import models.DbWrapper;
import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by recoilme on 19/03/14.
 */
public class Register extends Controller {

    public static void index() {

        OrientGraph graph = DbWrapper.dbFactory.getTx();
        for (Vertex v : graph.getVertices()) {
            System.out.println(v.toString());
            Set<String> properties = v.getPropertyKeys();
            for (String key:properties) {
                System.out.println(":"+key+":"+v.getProperty(key));
            }
            //graph.removeVertex(v);
        }
        graph.shutdown();

        render();
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
        if (email.length()<=3) {
            flash.error("Error: email length must be more then 3");
            index();
        }
        if (password.length()<5) {
            flash.error("Error: password length must be more then 5");
            index();
        }
        if (DbWrapper.getVertex("User.email", email)!=null) {
            flash.error("Error: user with this email:'"+ email + "' allready exists in database");
            index();
        }
        if (DbWrapper.getVertex("User.username", username)!=null) {
            flash.error("Error: user with this username:'"+ username + "' allready exists in database");
            index();
        }
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("username",username);
        props.put("email",email);
        props.put("password",password);
        props.put("remember",true);
        if (DbWrapper.addVertex("User", props)!=null) {

            session.clear();
            response.removeCookie("rememberme");
            session.put("username", username);

            Date expiration = new Date();
            String duration = Play.configuration.getProperty("secure.rememberme.duration","30d");
            expiration.setTime(expiration.getTime() + ((long) Time.parseDuration(duration)) * 1000L );
            response.setCookie("rememberme", Crypto.sign(username + "-" + expiration.getTime()) + "-" + username + "-" + expiration.getTime(),null,"/",
                    Time.parseDuration(duration),false,true);


            Application.index();

        }
        else {
            flash.error("Error: db commit failed");
            index();
        }
    }
}
