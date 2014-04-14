package controllers;


import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import models.DbWrapper;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoilme on 24/03/14.
 */
public class Post extends Controller {
    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            Vertex v = DbWrapper.getVertex(Security.connected().contains("@")?"User.email":"User.username", Security.connected());
            if (v != null) {
                renderArgs.put("username", v.getProperty("username"));
            }
        }
    }
    public static void newpost() {
        render();
    }

    public static void save(String content) {
        if(Security.isConnected()) {
            Vertex user = DbWrapper.getVertex(Security.connected().contains("@")?"User.email":"User.username", Security.connected());
            if (user != null) {
                Map<String,Object> props = new HashMap<String,Object>();
                PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES).and(Sanitizers.BLOCKS);
                content = policy.sanitize(content);
                props.put("content",content);
                props.put("modified",System.currentTimeMillis());
                Vertex article = DbWrapper.addVertex("Article", props);
                if (article!=null) {
                    Edge author = DbWrapper.addEdge("author",(ORID)user.getId(),(ORID)article.getId());
                    if (author!=null) {
                        Application.index();
                    }
                }
            }
        }

    }
}
