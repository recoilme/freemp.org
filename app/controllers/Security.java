package controllers;

import models.DbWrapper;

/**
 * Created by recoilme on 19/03/14.
 */
public class Security extends Secure.Security {

    static boolean authentify(String email, String password) {
        return true;
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

