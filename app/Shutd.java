import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import controllers.DbWrapper;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

/**
 * Created by recoil on 02.04.14.
 */
@OnApplicationStop
public class Shutd extends Job {
    public void doJob() {
        OrientGraph graph = DbWrapper.graph;
        graph.commit();
        graph.shutdown();

    }
}
