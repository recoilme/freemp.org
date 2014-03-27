package models;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Edge;
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
    public static OrientGraphFactory dbFactory = new OrientGraphFactory("plocal:/Users/recoil/orientdb-1.7/databases/fdb").setupPool(1,10);

    public static Vertex addVertex(String className, Map<String,Object> props) {
        OrientGraph graph = dbFactory.getTx();
        OrientVertex vertex = null;
        try {
            vertex = graph.addVertex("class:"+className);
            vertex.setProperties(props);
            graph.commit();
        }
        catch (Exception e) {
            System.out.println("Ex add vertex:"+e.toString());
            graph.rollback();
        }
        finally {
            graph.shutdown();
        }
        return vertex;
    }

    public static Edge addEdge(String edgeName, ORID vOut, ORID vIn) {
        OrientGraph graph = dbFactory.getTx();
        Edge edge = null;
        try {
            edge = graph.addEdge(null, graph.getVertex(vOut), graph.getVertex(vIn), edgeName);
            System.out.println("Created edge: " + edge.getId());
            graph.commit();
        }
        catch (Exception e) {
            System.out.println("Ex add edge:"+e.toString());
            graph.rollback();
        }
        finally {
            graph.shutdown();
        }
        return edge;
    }

    public static Vertex getVertex(String field, String value) {
        OrientGraph graph = dbFactory.getTx();
        Iterable<Vertex> results = null;
        try {
            results = graph.getVertices(field, value);
            if (results.iterator().hasNext()) {
                return results.iterator().next();
            }
        }
        catch (Exception e) {
            System.out.println("Ex get vertex:"+e.toString());
        }
        finally {
            graph.shutdown();
        }
        return null;
    }

    public static Iterable<Vertex> getQueryResult(String sql) {

        return null;
    }
}
