package http_file_trans;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest; 
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * A simple HTTP client that request a file from server and save it to the hard-disk.
 */
public class HttpClient {
    
    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(HttpClient.class);

    private final URI uri;

    public HttpClient(URI uri) {
        this.uri = uri;
    }

    public void run() {
    	
    	ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new HttpClientPipelineFactory());
		ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost",8080));
		Channel channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return;
        }
        HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath()); 
        channel.write(request);
        
		// Wait until the connection is closed or the connection attempt fails. 
		channel.getCloseFuture().awaitUninterruptibly();
		
		//shut down thread pools to exit
		bootstrap.releaseExternalResources();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error(
                    "Usage: " + HttpClient.class.getSimpleName() +
                    " <URL>");
            return;
        }
        URI uri = new URI(args[0]);
        //URI uri = new URI("http://localhost:8080/test.mp3");
        new HttpClient(uri).run();
        
    }
}