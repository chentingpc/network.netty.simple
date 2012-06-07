package http_file_trans;

import java.io.File;
import java.io.FileOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.CharsetUtil;

public class HttpClientHandler extends SimpleChannelUpstreamHandler {
    
    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(HttpClientHandler.class);

    private boolean readingChunks;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	 
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();

            logger.info("STATUS: " + response.getStatus());
            logger.info("VERSION: " + response.getProtocolVersion());
            
            if (!response.getHeaderNames().isEmpty()) {
                for (String name: response.getHeaderNames()) {
                    for (String value: response.getHeaders(name)) {
                        logger.info("HEADER: " + name + " = " + value);
                    }
                }
            }

            if (response.isChunked()) {
                readingChunks = true;
                logger.info("CHUNKED CONTENT {");
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    logger.info("CONTENT {");
                    //logger.info(content.toString(CharsetUtil.UTF_8));
                    logger.info("} END OF CONTENT");
                }
                
                //save the file
            	String filename="test2.mp3";
            	File file;
            	FileOutputStream saver = null;
            	file=new File(filename);  
            	if(!file.exists()){
            	    file.createNewFile();  
            	    saver=new FileOutputStream(file);
            	}
            	else
            		return;
            	byte c;
            	while ( content.readable() ){
        			c = content.readByte();
        			saver.write(c); 
        		}   
            	saver.close(); 
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                logger.info("} END OF CHUNKED CONTENT");
            } else {
                logger.info(chunk.getContent().toString(CharsetUtil.UTF_8));
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause(); 
        cause.printStackTrace(); 
    }
}