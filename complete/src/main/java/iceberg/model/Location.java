package iceberg.model;

import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

public interface Location extends VertexFrame {
    
    @Property("name")
    public String getName();
    
    @Property("name")
    public String setName(String name);
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    @Property("latlong")
    public Geoshape getLocation();
    
    @Property("latlong")
    public Geoshape setLocation(Geoshape location);
}
