package telnet;

import java.net.InetAddress; 
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * Handles a server-side channel.
 */
public class TelnetServerHandler extends SimpleChannelUpstreamHandler { 
	 
    private static final Logger logger = Logger.getLogger(
            TelnetServerHandler.class.getName());
    
    //@SuppressWarnings("unused")
	private Channel [] ChannelList;  
	private int pos;
	
    public TelnetServerHandler(Channel [] channellist){
    	ChannelList=channellist;
    	pos=ChannelList.length;
    }
    
    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Send greeting for a new connection.
        e.getChannel().write(
                "Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        e.getChannel().write("It is " + new Date() + " now.\r\n"); 
        
        //remember the client in the channel list
        boolean record=false;  
    	for (int i=0;i<ChannelList.length;i++){
    			if (ChannelList[i]==null && pos==ChannelList.length){
    				pos=i;
    			}
    			if (ChannelList[i]==e.getChannel()){
    				record=true; 
    				pos=i;
    				break;
    			} 
    	} 
    	if (record==false){
    		if (pos>=ChannelList.length){
    			System.out.print("No more buffer!\n");
    			return;
    		}
    		
    		ChannelList[pos]=e.getChannel();
    	} 
    	
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {  
    	
        // Cast to a String first.
        // We know it is a String because we put some codec in TelnetPipelineFactory.
        String request = (String) e.getMessage();

        // Generate and write a response.
        String response;
        boolean close = false;
        if (request.length() == 0) {
            response = "Please type something.\r\n";
        } else if (request.toLowerCase().equals("bye")) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            response = "You say: " + request + "\r\n";
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        //ChannelFuture future = e.getChannel().write(response); 
        ChannelFuture future = e.getChannel().write(response); 
        for (int i=0;i<ChannelList.length;i++){
        	if (i!=pos && ChannelList[i]!=null){
        		ChannelList[i].write(pos+" says: "+request+"\r\n");
        	}
        }
        
        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
           future.addListener(ChannelFutureListener.CLOSE);
           ChannelList[pos]=null;
        }
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}
