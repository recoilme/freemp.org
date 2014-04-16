import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import controllers.DbWrapper;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * Created by recoil on 02.04.14.
 */
@OnApplicationStart
public class Boot extends Job {


    public void doJob() {
        OrientGraph graph = DbWrapper.graph;
        // Check if the database is empty

    }
    /*

    @Before
    static void addDb() {
        OrientGraph graph = dbFactory.getTx();
        try {
            if (false) {
                for (Vertex v : graph.getVertices()) {
                    graph.removeVertex(v);
                }
                graph.commit();
            }

            graph.createKeyIndex("name", Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", "Usr"));
            graph.commit();

            OrientVertex u = graph.addVertex("class:Usr", "name", "admin", "email", "", "pwd", "admin");
            OrientVertex p = graph.addVertex( "class:Post", "title", "title", "body", "body");
            p.addEdge("class:Author",u,null,null,"date","2013-07-30");
            graph.commit();
        }
        catch (Exception e) {
            System.out.println("Error:|"+e.toString()+"|");
            graph.rollback();
        }
        finally {
            graph.shutdown();
        }
    }
    */
}
