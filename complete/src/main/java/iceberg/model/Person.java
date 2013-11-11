package iceberg.model;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import hello.*;

public interface Person extends VertexFrame {
    
    @Property("name")
    public String getName();
    
    @Property("name")
    public String setName(String name);
    
    public long getId();
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    @Adjacency(label = "family") Iterable<? extends Person> getFamily();
    
    @Adjacency(label = "family") void addFamily(Person person);
    
    @Adjacency(label = "friends") Iterable<? extends Person> getFriends();
    
    @Adjacency(label = "friends") void addPet(Person person);
}
