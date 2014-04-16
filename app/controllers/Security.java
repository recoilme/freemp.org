package controllers;

import com.tinkerpop.blueprints.Vertex;
import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Http;

import java.util.Date;

/**
 * Created by recoilme on 19/03/14.
 */
public class Security extends Secure.Security {

    static boolean authenticate(String email, String password) {
        Vertex v = DbWrapper.getVertex("ClsUser.email", email);
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
        if (session!=null && session.contains("username")) {
            Vertex vUser = DbWrapper.getVertex("ClsUser.email", session.get("username"));
            if (vUser != null) {
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
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

}

