package iceberg.model;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

public interface Story extends VertexFrame {
    
    @Property("name")
    public String getName();
    
    @Property("name")
    public String setName(String name);
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    @Adjacency(label = "Media") Iterable<? extends Media> getMedia();
    
    @Adjacency(label = "Media") void addNedia(Media media);    

}
