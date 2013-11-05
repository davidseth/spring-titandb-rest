package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tinkerpop.blueprints.Graph;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

@Controller
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public @ResponseBody Greeting greeting(
            @RequestParam(value="name", required=false, defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
    
    @RequestMapping("/graph")
    public @ResponseBody String graph() {
        
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandra");
        conf.setProperty("storage.hostname","127.0.0.1");
        
        TitanGraph g = TitanFactory.open(conf);
        //Graph graph = new TitanFactory()
      
        
        Vertex juno = g.addVertex(null);
        juno.setProperty("name", "juno");
        juno.setProperty("city", "bismarck");
        Vertex jupiter = g.addVertex(null);
        jupiter.setProperty("name", "jupiter");
        Edge married = g.addEdge(null, juno, jupiter, "married");        
        
        return juno.toString();
        //return "hello";
    }
}