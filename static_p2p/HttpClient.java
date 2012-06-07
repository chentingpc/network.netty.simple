package p2p;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * A simple p2p file share network
 */
public class HttpClient {
    
    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(HttpClient.class);

    private final URI uri;

    public HttpClient(URI uri) {
        this.uri = uri;
    }

    public void run(String filename) {
    	
    	ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new HttpClientPipelineFactory(filename)); 
        String host = uri.getHost() == null? "localhost" : uri.getHost();
        int port = uri.getPort();
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,port));
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
                    " <yourPort>");
            return;
        } 
        
        //run the file server first
        int serverPort=Integer.parseInt(args[0]);
        //int serverPort=123;
        //java.util.Random r=new java.util.Random();
        //while ( (serverPort=r.nextInt()) < 0);
        System.out.print("File server on Port "+serverPort+"\n");
        Runtime.getRuntime().exec("java -jar HttpStaticFileServer.jar "+serverPort);
        
        //loop wait for file request
        String request = null;
        for (;;){
        	//delete the location file first
        	FileInputStream reader = null;
          	File file=new File("location");  
          	if(file.exists())
          	    file.delete();
        
			 try{
				 BufferedReader in=new BufferedReader(
				 new InputStreamReader(System.in));
				 System.out.println("请输入文件名：");
				 request=in.readLine(); 
				 //request="test";
				 if (request ==  null)
					 continue;
				 System.out.print("your request is "+request+"\n");
			   }
			  catch(IOException ioe)
			  {
				 ioe.printStackTrace();
			  } 
			  URI uri = new URI("http://localhost:8080/"+request); 
			  new HttpClient(uri).run(null); 
			  int i=0;
		      int max_time=10;
			  for (;i<max_time;i++){
				  if(file.exists()){
			      	  reader=new FileInputStream(file);
			      	  break;
			      } 
				  Thread.sleep(1000);
		      }
		      if (i==max_time){
		    	  System.out.print("File Not exsit!\n");
			      continue;
		      }
		    	 
		      //read in information
		      byte c;
		      char [] info= new char[256];
		      String location = null;
		      int tempi=0;
		      while ( (c = (byte) reader.read()) != -1){
		    	  info[tempi]=(char) c; 
		    	  tempi++;
		      }  
		      location=String.valueOf(info, 0, tempi);  
		      if (location.length() < 7 || location.substring(0, 7).equals("Failure")){
		    	  System.out.print("File not exsit!\n");
		    	  continue;
		      }
		      System.out.println("file \""+request+"\" location @ "+location);
      		  reader.close();
      		  file.delete();
      		  
      		  //get the real file 
      		  System.out.println("http://"+location+"/"+request);
      		  URI uri2 = new URI("http://"+location+"/"+request);
			  new HttpClient(uri2).run(request);
        }   
    }
}