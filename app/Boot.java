import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import models.DbWrapper;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * Created by recoil on 02.04.14.
 */
@OnApplicationStart
public class Boot extends Job {


    public void doJob() {
        OrientGraph graph = DbWrapper.graph;
        // Check if the database is empty

    }

}
