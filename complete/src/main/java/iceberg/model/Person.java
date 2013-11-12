package iceberg.model;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.blueprints.Vertex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

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
    
    @GremlinGroovy("it.as('x').out('created').in('created').except('x')")
    public Iterable<Person> getCoCreators();  
    
    public abstract class Impl implements JavaHandlerContext<Vertex>, Person {
//        public String getNameAndAge() {
//          return getName() + " (" + getAge() + ")"; //Call other methods that are handled by other annotations.
//        } 
        
        @Override
        @JavaHandler
        public Object getId() {
            Vertex vertex = this.asVertex();
          Object id = this.it().getId();
          return id;
        }        
    }
}
