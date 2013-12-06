package iceberg.model;

import iceberg.edge.Family;
import iceberg.edge.Friends;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.blueprints.Vertex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("type")
public interface Person extends VertexFrame {
    
    @Property("name")
    public String getName();
    
    @Property("name")
    public String setName(String name);
    
//    @Property("id")
//    @GremlinGroovy(value="it.asVertex().getId()", frame=false)
//    public long getId();
    
    @Property("id")
    @JavaHandler
    public Object getId();    
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    @Adjacency(label = "family")
    Iterable<Person> getFamily();
    //Iterable<? extends Person> getFamily();
    
    @Adjacency(label = "family")
    void addFamily(Person person);
    
    @Adjacency(label = "friends")
    Iterable<Person> getFriends();
    //Iterable<? extends Person> getFriends();
    
    @Adjacency(label = "friends")
    void addFriend(Person person);
    
    //@JsonIgnore
    @Adjacency(label = "friends")
    @GremlinGroovy("it.as('x').out('friends').in('friends').except('x')")
    public Iterable<Person> getFriendsOfFriends();  
    
    public abstract class Impl implements JavaHandlerContext<Vertex>, Person {
//        public String getNameAndAge() {
//          return getName() + " (" + getAge() + ")"; //Call other methods that are handled by other annotations.
//        } 
        
        @Override
        @JavaHandler
        public Object getId() {
            return this.asVertex().getId(); 
            //Object id = it().getId();
            
//            Vertex vertex = this.asVertex();
//          Object id = this.it().getId();
          //return id;
          //return it().getProperty("id");
        }
        
        @Initializer
        public void init() {
           //This will be called when a new framed element is added to the graph.
           setContent("This is placeholder content if nothing explicit is set!!!");
        }        
    }
}
