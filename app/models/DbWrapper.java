package models;

import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by recoilme on 20/03/14.
 */
public class DbWrapper {


    public static OrientGraph graph = new OrientGraphFactory("plocal:fdb").setupPool(1,10).getTx();

    public static Vertex addVertex(String className, Map<String,Object> props) {

        OrientVertex vertex = null;
        try {
            vertex = graph.addVertex("class:"+className);
            vertex.setProperties(props);
            //graph.commit();
        }
        catch (Exception e) {
            System.out.println("Ex add vertex:"+e.toString());
            //graph.rollback();
        }
        finally {
            //graph.shutdown();
        }
        return vertex;
    }

    public static Vertex saveClass(Object object){
        Class clazz = object.getClass();
        Map<String,Object> props = new HashMap<String,Object>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                props.put(field.getName(),field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return addVertex(clazz.getSimpleName(),props);
    }

    public static Edge addEdge(String edgeName, ORID vOut, ORID vIn) {
        //OrientGraph graph = dbFactory.getTx();
        Edge edge = null;
        try {
            edge = graph.addEdge(null, graph.getVertex(vOut), graph.getVertex(vIn), edgeName);
            graph.commit();
        }
        catch (Exception e) {
            System.out.println("Ex add edge:"+e.toString());
            graph.rollback();
        }
        finally {
            //graph.shutdown();
        }
        return edge;
    }

    public static Vertex getVertex(String field, String value) {
        //OrientGraph graph = dbFactory.getTx();
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
            //graph.shutdown();
        }
        return null;
    }

    public static OrientGraph getGraph() {
        return graph;
    }

    public static String Vertex2String(Vertex v) {
        StringBuilder result = new StringBuilder();
        if (v!=null) {
            Set<String> propertys = v.getPropertyKeys();
            for (String key: propertys){
                if (!result.toString().equals("")) {
                    result.append(",");
                }
                result.append(key);
                result.append(":");
                result.append(v.getProperty(key));
            }
        }
        else {
            result.append("null");
        }
        return result.toString();
    }
}
