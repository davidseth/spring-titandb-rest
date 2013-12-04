/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package iceberg.edge;

import iceberg.model.*;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.frames.EdgeFrame;

import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;


import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;


/**
 *
 * @author david
 */
public interface Friends extends EdgeFrame {

    @OutVertex
    public Person getOut();

    @InVertex
    public Person getIn();    
    
//    @DateProperty ("createdDate")
//    public void setCreatedDate(Date createdDate);
//
//    @DateProperty("createdDate")
//    public Date getCreatedDate();
        
    @JavaHandler
    public String getNames();
    
    
    abstract class Impl implements Friends, JavaHandlerContext<Edge> {

            @Override
            @JavaHandler
            public String getNames() {
                    return getOut().getName() + "<->" + getIn().getName();
            }
    }    
}


