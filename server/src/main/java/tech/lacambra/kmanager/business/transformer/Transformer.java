package tech.lacambra.kmanager.business.transformer;

import java.io.InputStream;
import java.util.List;

public interface Transformer<T> {
    
    T transform(List<InputStream> inputStreams);
    
    String getSupportedInputType();
    
    String getOutputType();
}