package telnet;

import java.io.BufferedReader;
import java.io.IOException;  
import java.io.InputStreamReader;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * Handles a client-side channel.
 */
public class TelnetClientHandler extends SimpleChannelUpstreamHandler {	
	
    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(TelnetClientHandler.class);

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        // Print out the line received from the server.
        logger.info((String) e.getMessage());  
    } 

	@Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn(
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}