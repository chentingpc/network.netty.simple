package p2p;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class HttpClientPipelineFactory implements ChannelPipelineFactory {
	
	private String filename = null;
	HttpClientPipelineFactory(String filename){
		this.filename  = filename;
	}
	
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
 
        pipeline.addLast("decoder", new HttpResponseDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(104857600));//maximum 100MB file allowed
        pipeline.addLast("encoder", new HttpRequestEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", new HttpClientHandler(filename));
        return pipeline;
    }
}