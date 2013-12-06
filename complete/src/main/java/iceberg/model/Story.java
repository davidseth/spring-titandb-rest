package iceberg.model;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

public interface Story extends VertexFrame {
    
    @Property("title")
    public String getTitle();
    
    @Property("title")
    public String setTitle(String title);
    
    @Property("content")
    public String getContent();
    
    @Property("content")
    public String setContent(String content);
    
    @Adjacency(label = "Media") Iterable<? extends Media> getMedia();
    
    @Adjacency(label = "Media") void addNedia(Media media);    

}
