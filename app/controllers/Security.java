package controllers;

import com.tinkerpop.blueprints.Vertex;
import models.DbWrapper;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by recoilme on 19/03/14.
 */
public class Security extends Secure.Security {

    static boolean authenticate(String email, String password) {
        Vertex v = DbWrapper.getVertex("User.email", email);
        if (v != null && v.getProperty("password").equals(password)) {

            return true;
        }
        return false;
    }

    static boolean check(String profile) {
        if("admin".equals(profile)) {
            return false;//User.find("byEmail", connected()).<User>first().isAdmin;
        }
        return false;
    }

    static void onDisconnected() {
        Application.index();
    }

    static void onAuthenticated() {
        Application.index();
    }

}

