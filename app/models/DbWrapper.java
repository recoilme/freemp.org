package models;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by recoilme on 20/03/14.
 */
public class DbWrapper {
    static OrientGraphFactory dbFactory = new OrientGraphFactory("plocal:/Users/recoilme/tmp/db").setupPool(1,10);

    public static boolean has(String field, String value) {
        OrientGraph graph = dbFactory.getTx();
        Iterable<Vertex> results = null;
        try {
            results = graph.getVertices(field, value);
            if (results.iterator().hasNext()) {
                return true;
            }
        }
        catch (Exception e) {
            System.out.println("Ex:"+e.toString());

        }
        finally {
            graph.shutdown();
        }
        return false;
    }

    public static boolean addVertex(String className, Map<String,Object> props) {
        OrientGraph graph = dbFactory.getTx();
        try {
            OrientVertex vertex = graph.addVertex("class:"+className);
            vertex.setProperties(props);
            graph.commit();
            return true;
        }
        catch (Exception e) {
            System.out.println("Ex:"+e.toString());
            graph.rollback();
        }
        finally {
            graph.shutdown();
        }
        return false;
    }
}
