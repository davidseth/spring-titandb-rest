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
    
    @Adjacency(label = "pet") Iterable<? extends Animal> getPets();
    
    @Adjacency(label = "pet") void addPet(Animal animal);    
}
