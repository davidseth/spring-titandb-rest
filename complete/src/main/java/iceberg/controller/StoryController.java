package iceberg.controller;

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
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility;

import iceberg.model.Person;
import iceberg.model.Location;
import iceberg.model.Story;
import org.springframework.http.HttpStatus;

@Controller
public class StoryController {

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
    
    @RequestMapping(value="/story/{id}", method=RequestMethod.GET)
    public @ResponseBody Story getStory(@PathVariable Long id) {
        FramedGraph<TitanGraph> framedGraph = getFramedTitanGraph();
        
        Story story = framedGraph.getVertex(id, Story.class);

        return story;
    }
    
    @RequestMapping(value="/story-vertex/{id}", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody String getStoryAsVertex(@PathVariable Long id) throws JSONException {
        TitanGraph g = getTitanGraph();
        
        com.thinkaurelius.titan.core.TitanVertex vertex = (com.thinkaurelius.titan.core.TitanVertex) g.getVertex(id);
        //return vertex;
        
        JSONObject json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, GraphSONMode.EXTENDED);
        System.out.println(json.toString());
        return json.toString();
    }
    
    // mapping help: http://www.byteslounge.com/tutorials/spring-mvc-requestmapping-example
    @RequestMapping(value="/story/add", method=RequestMethod.POST)
    @ResponseStatus( HttpStatus.CREATED )
    public Story addStory(@RequestBody Story story) {
        FramedGraph<TitanGraph> framedGraph = getFramedTitanGraph();
        System.out.println(story.toString());
        Story story2 = framedGraph.addVertex(null, Story.class);
        story2.setTitle(story.getTitle());
        
        framedGraph.getBaseGraph().commit();

        return story;//.asVertex().getId();
    }
}