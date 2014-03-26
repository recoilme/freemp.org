package controllers;


import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import models.DbWrapper;
import play.mvc.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoilme on 24/03/14.
 */
public class Post extends Controller {

    public static void newpost() {
        render();
    }

    public static void save(String content) {
        if(Security.isConnected()) {
            Vertex user = DbWrapper.getVertex(Security.connected().contains("@")?"User.email":"User.username", Security.connected());
            if (user != null) {
                Map<String,Object> props = new HashMap<String,Object>();
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
