package controllers;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import models.DbWrapper;
import play.Play;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoilme on 19/03/14.
 */
public class Register extends Controller {
    static OrientGraphFactory dbFactory = new OrientGraphFactory("plocal:/Users/recoilme/tmp/db").setupPool(1,10);

    public static void index() {

        OrientGraph graph = dbFactory.getTx();
        for (Vertex v : graph.getVertices()) {
            //graph.removeVertex(v);
            System.out.println("User:"+v.getProperty("username"));
        }

        render();
    }

    public static void save(String username, String email, String password, boolean remember) {
        if (DbWrapper.has("User.email", email)) {
            flash.error("Error: user with this email:'"+ email + "' allready exists in database");
            index();
        }
        if (DbWrapper.has("User.username", username)) {
            flash.error("Error: user with this username:'"+ username + "' allready exists in database");
            index();
        }
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("username",username);
        props.put("email",email);
        props.put("password",password);
        props.put("remember",remember);
        if (DbWrapper.addVertex("User", props)) {
            Security.authentify(email,password);
            Application.index();
        }
        else {
            flash.error("Error: db commit failed");
            index();
        }
    }
}
