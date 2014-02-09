package hello;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;


import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanType;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;



import iceberg.model.Person;
import iceberg.model.Location;
import iceberg.model.Story;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

@Controller
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    //private FramedGraph<Graph> framedGraph;

    private Configuration getTitanConf() {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandrathrift");
        conf.setProperty("storage.hostname","127.0.0.1");
        
        conf.setProperty("storage.index.search.backend", "elasticsearch");
        conf.setProperty("storage.index.search.hostname", "127.0.0.1");
        conf.setProperty("storage.index.search.client-only", "true");        
        
        return conf;
    }
    
    private TitanGraph getTitanGraph() {
        TitanGraph g = TitanFactory.open(getTitanConf());
        
        return g;
    }
    

    /*
     * 
     */
    private FramedGraph<TitanGraph> getFramedTitanGraph() {
        TitanGraph g = getTitanGraph();
        
        FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule(), new JavaHandlerModule());
        FramedGraph<TitanGraph> framedGraph = factory.create(g);        
        
        return framedGraph;
    }    
    
    @RequestMapping(value="/person/{id}", method=RequestMethod.GET)
    @ResponseBody
    public Person getPerson(@PathVariable Long id) {
        FramedGraph<TitanGraph> framedGraph = getFramedTitanGraph();
        
        Person nicola = framedGraph.getVertex(id, Person.class);

        return nicola;
    }
    
    @RequestMapping(value="/person-vertex/{id}", method=RequestMethod.GET, produces="application/json")
    @ResponseBody
    public String getPersonAsVertex(@PathVariable Long id) throws JSONException {
        TitanGraph g = getTitanGraph();
        
        com.thinkaurelius.titan.core.TitanVertex vertex = (com.thinkaurelius.titan.core.TitanVertex) g.getVertex(id);
        //return vertex;
        
        JSONObject json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, GraphSONMode.EXTENDED);
        System.out.println(json.toString());
        return json.toString();
    }
    
    // mapping help: http://www.byteslounge.com/tutorials/spring-mvc-requestmapping-example
    @RequestMapping(value="/person/add/{name}", method=RequestMethod.GET)
    @ResponseStatus( HttpStatus.CREATED )
    @ResponseBody
    public Person addPerson(@PathVariable("name") String name) {
        FramedGraph<TitanGraph> framedGraph = getFramedTitanGraph();

        Person nicola = framedGraph.addVertex(null, Person.class);
        nicola.setName(name);
        
        framedGraph.getBaseGraph().commit();

        return nicola;//.asVertex().getId();
    } 
    
    public static FrameInitializer myInitializer = new FrameInitializer() {
        @Override
        public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element) {
            element.setProperty("class", kind.getName()); //For example: save the class name of the frame on the element
        }
    };   
    
    @RequestMapping("/graph")
    @ResponseBody
    public String graph() {
        FramedGraph<TitanGraph> framedGraph = getFramedTitanGraph();
        TitanGraph g = framedGraph.getBaseGraph();
        StringBuilder output = new StringBuilder();
        
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
        TitanType locationType = g.getType("location");
        if (locationType == null) {
            g.makeKey("location").dataType(Geoshape.class).indexed(Vertex.class).indexed(Edge.class).indexed("search",Vertex.class).indexed("search",Edge.class).make();
        }     
        
        for (String key : g.getIndexedKeys(Vertex.class)) {
            TitanKey type = (TitanKey) g.getType(key);
            //dump the key
           
            System.out.println("found key: " + key);
            System.out.println("\tname: " + type.getName());
            System.out.println("\ttype: " + type.getDataType());
            System.out.println("\tid: " + type.getId());
            System.out.println("\tisNew: " + type.isNew());
            System.out.println("\tisModifiable: " + type.isModifiable());
            System.out.println("\tisUnique: " + type.isUnique(Direction.IN));
            System.out.println("\tisPropertyKey: " + type.isPropertyKey());
            
            for (String indice : type.getIndexes(Vertex.class)) {
                System.out.println("\t\tindex: " + indice);
            }
        }
        
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
        
       
        Location location = framedGraph.addVertex(null, Location.class);
        location.setName("Palmwoods");
        Geoshape shape = Geoshape.point(-26.68584, 152.96135);
        location.setLocation(shape);

        Person nicola = (Person) framedGraph.addVertex(null, Person.class);
        nicola.setName("Nicola Peterson");        
       
        Person david = (Person) framedGraph.addVertex(null, Person.class);
        david.setName("David Peterson");
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("street", 1);
        map.put("stuff", 2);
        map.put("cars", 3);

        david.asVertex().addEdge("lives", location.asVertex());
        david.asVertex().setProperty("address", map );
        david.addFamily(nicola);
        david.addFriend(nicola);
        
        //JSONObject json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(david, null, GraphSONMode.EXTENDED);
        output.append(david);
       
        
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
        
        
        for(Vertex vertex : juno.query().vertices()) { 
            System.out.println(" -- " + vertex.getProperty("name"));
            output.append( " -- " + vertex.getProperty("name"));
        }        
        
        return output.toString();
    }
}