package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import com.tinkerpop.blueprints.Graph;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanType;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import iceberg.model.Person;

@Controller
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    //private FramedGraph<Graph> framedGraph;

    @RequestMapping("/greeting")
    public @ResponseBody Greeting greeting(
            @RequestParam(value="name", required=false, defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
    
    @RequestMapping(value="/person/{id}", method=RequestMethod.GET)
    public @ResponseBody Person getPerson(@PathVariable Long id) {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandrathrift");
        conf.setProperty("storage.hostname","127.0.0.1");
        TitanGraph g = TitanFactory.open(conf);
        
        FramedGraphFactory factory = new FramedGraphFactory();
        FramedGraph<TitanGraph> framedGraph = factory.create(g);

        Person nicola = framedGraph.getVertex(id, Person.class);

        
        return nicola;
    }     
    
    // mapping help: http://www.byteslounge.com/tutorials/spring-mvc-requestmapping-example
    @RequestMapping(value="/person/add/{name}", method=RequestMethod.GET)
    public @ResponseBody Person addPerson(@PathVariable("name") String name) {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandrathrift");
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
    
    public static FrameInitializer myInitializer = new FrameInitializer() {
        @Override
        public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element) {
            element.setProperty("class", kind.getName()); //For example: save the class name of the frame on the element
        }
    };   
    
    @RequestMapping("/graph")
    public @ResponseBody String graph() {
        
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandrathrift");
        conf.setProperty("storage.hostname","127.0.0.1");
        
        conf.setProperty("storage.index.search.backend", "elasticsearch");
        conf.setProperty("storage.index.search.hostname", "127.0.0.1");
        conf.setProperty("storage.index.search.client-only", "true");
     
        TitanGraph g = TitanFactory.open(conf);
        
        //TitanTransaction trans = g.newTransaction();
        
        TitanType nameType = g.getType("name");
        if (nameType == null) {
            g.makeKey("name").dataType(String.class).indexed(Vertex.class).make();
            g.makeLabel("place").make();
            g.makeLabel("married").make();
            
        }
        TitanType cityType = g.getType("city");
        if (cityType == null) {
            g.makeKey("city").dataType(String.class).indexed(Vertex.class).indexed(Edge.class).indexed("search",Vertex.class).indexed("search",Edge.class).make();
        }

        
        
//        FramedGraph<TitanGraph> framedGraph = new FramedGraph(g);
//        framedGraph.registerFrameInitializer(myInitializer);
        
        FramedGraphFactory factory = new FramedGraphFactory(new JavaHandlerModule());
        FramedGraph<TitanGraph> framedGraph = factory.create(g);

//        FramedGraphFactory factory = new FramedGraphFactory(
//            new GremlinGroovyModule(),
//            new TypedGraphModuleBuilder().withClass(Person.class).build());
//        FramedGraph framedGraph = factory.create(g);  //Groovy and typed graph created.         
//        
        
//        framedGraph = new FramedGraphFactory(new AbstractModule(){
//                @Override
//                protected void doConfigure(FramedGraphConfiguration config) {
//                 config.addFrameInitializer(myInitializer);
//                }
//        }).create(g);        
        
       

        Person nicola = (Person) framedGraph.addVertex(null, Person.class);
        nicola.setName("Nicola Peterson");        
       
        Person david = (Person) framedGraph.addVertex(null, Person.class);
        david.setName("David Peterson");
        david.addFamily(nicola);
        david.addFriend(nicola);
       
        
        System.out.println( "david id: " + david.getId());
        System.out.println( "nicola content: " + nicola.getName());

//        if (g.isOpen() == false) {
//            g = TitanFactory.open(conf);
//            return "";
//        }
        
        
        
        
        Vertex juno = g.addVertex(null);
        juno.setProperty("name", "juno");
        juno.setProperty("city", "bismarck");
        juno.setProperty("content", "why won't this work???");
        
        Vertex jupiter = g.addVertex(null);
        jupiter.setProperty("name", "jupiter");
        Edge friends = g.addEdge(null, juno, jupiter, "friends");
        Edge family = g.addEdge(null, juno, jupiter, "family");
        
        Person jupiterFrame = framedGraph.getVertex(jupiter.getId(), Person.class);
        System.out.println( "jupiterFrame id: " + jupiterFrame.asVertex().getId());
        System.out.println( "jupiterFrame content: " + jupiterFrame.getName());        
        
        
        
        //return juno.toString();

        System.out.println( "juno id: " + juno.getId());
       
        
        Vertex turnus = g.addVertex(null);
        turnus.setProperty("name", "turnus");
        Vertex hercules = g.addVertex(null);
        hercules.setProperty("name", "hercules");
        Vertex dido = g.addVertex(null);
        dido.setProperty("name", "dido");
        Vertex troy = g.addVertex(null);
        troy.setProperty("name", "troy");
        jupiter.setProperty("name", "jupiter");
        
        Edge friends2 = g.addEdge(null, juno, turnus, "friends");

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