package iceberg.model;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

public interface Media extends VertexFrame {
    
    @Property("name")
    public String getName();
    
    @Property("name")
    public String setName(String name);
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    
    @Property("url")
    public String getURL();
    
    @Property("url")
    public String setURL(String name);

}
