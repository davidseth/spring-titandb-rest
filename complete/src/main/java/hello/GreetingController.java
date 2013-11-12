package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tinkerpop.blueprints.Graph;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import iceberg.model.Person;

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
    
    @RequestMapping("/person")
    public @ResponseBody Person getPerson(@RequestParam(value="id", required=true) Long id) {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandra");
        conf.setProperty("storage.hostname","127.0.0.1");
        TitanGraph g = TitanFactory.open(conf);
        
        FramedGraphFactory factory = new FramedGraphFactory();
        FramedGraph<TitanGraph> framedGraph = factory.create(g);

        Person nicola = framedGraph.getVertex(id, Person.class);

        
        return nicola;
    }     
    
    @RequestMapping("/person/add")
    public @ResponseBody Person addPerson(@RequestParam(value="name", required=false, defaultValue="World") String name) {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandra");
        conf.setProperty("storage.hostname","127.0.0.1");
        TitanGraph g = TitanFactory.open(conf);
        
        //TitanTransaction trans = g.newTransaction();
        
        FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule());
        FramedGraph<TitanGraph> framedGraph = factory.create(g);

        Person nicola = framedGraph.addVertex(null, Person.class);
        nicola.setName(name);
        
        g.commit();
        
        return nicola;//.asVertex().getId();
    } 
    
    @RequestMapping("/graph")
    public @ResponseBody String graph() {
        
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandra");
        conf.setProperty("storage.hostname","127.0.0.1");
        
//        conf.setProperty("storage.index.search.backend", "elasticsearch");
//        conf.setProperty("storage.index.search.hostname", "127.0.0.1");
//        conf.setProperty("storage.index.search.client-only", "true");
        
        
     
        TitanGraph g = TitanFactory.open(conf);
        
        //TitanTransaction trans = g.newTransaction();
        
        g.makeKey("name").dataType(String.class).indexed(Vertex.class).make();
        g.makeLabel("place").make();
        g.makeLabel("married").make();
        
        //g.makeKey("city").dataType(String.class).indexed(Vertex.class).indexed(Edge.class).indexed("search",Vertex.class).indexed("search",Edge.class).make();
                
        //Graph graph = new TitanFactory()
      
        //TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraphFactory factory = new FramedGraphFactory();
        FramedGraph<TitanGraph> framedGraph = factory.create(g);

        Person nicola = framedGraph.addVertex(null, Person.class);
        nicola.setName("Nicola Peterson");        
       
        Person david = framedGraph.addVertex(null, Person.class);
        david.setName("David Peterson");
        david.addFamily(nicola);
        
        
        Vertex juno = g.addVertex(null);
        juno.setProperty("name", "juno");
        juno.setProperty("city", "bismarck");
        Vertex jupiter = g.addVertex(null);
        jupiter.setProperty("name", "jupiter");
        Edge married = g.addEdge(null, juno, jupiter, "married");   
        
        //return juno.toString();
        
        
       

        System.out.println(juno.toString());
       
        
        Vertex turnus = g.addVertex(null);
        turnus.setProperty("name", "turnus");
        Vertex hercules = g.addVertex(null);
        hercules.setProperty("name", "hercules");
        Vertex dido = g.addVertex(null);
        dido.setProperty("name", "dido");
        Vertex troy = g.addVertex(null);
        troy.setProperty("name", "troy");
        jupiter.setProperty("name", "jupiter");

        Edge edge = g.addEdge(null, juno, turnus, "knows");
        edge.setProperty("since",2008);
        edge.setProperty("stars",5);
        edge = g.addEdge(null, juno, hercules, "knows");
        edge.setProperty("since",2011);
        edge.setProperty("stars",1);
        edge = g.addEdge(null, juno, dido, "knows");
        edge.setProperty("since", 2011);
        edge.setProperty("stars", 5);
        g.addEdge(null, juno, troy, "likes").setProperty("stars",5);        
        
        Iterable<Vertex> results = juno.query().labels("knows").has("since",2011).has("stars",5).vertices();
        
        Object results2 = troy.query().labels("knows").has("stars", 5).vertexIds();

        g.commit();
        
        String output = new String();
        for(Vertex vertex : juno.query().vertices()) { 
            System.out.println(" -- " + vertex.getProperty("name"));
            output += " -- " + vertex.getProperty("name");
        }        
        
        return output;
    }
}