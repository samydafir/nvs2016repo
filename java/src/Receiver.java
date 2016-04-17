import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Receiver represents a UDP packet receiver. Saves content of received packets to
 * receive.txt in the directory the class file is in.
 * @author Thomas Samy Dafir, 1331483
 * @author Dominik Baumgartner, 0920177
 * @author Vivien Wallner, 1320293
 */
public class Receiver {
    
	//setting MAXSIZE to 100
    private final static int SIZE = 2000;
    /**
     * main handles wrong numbers of cmd-arguments. Wrong numbers lead to a message describing usage being
     * issued, otherwise receive() is called. IOExceptions thrown in the receive-method are handled here.
     * @param args cmd-arguments:
     * 	args[0] port number
     * 	args[1] timeout for the receiving socket
     */
	public static void main(String[] args){
		if(args.length != 4 && args.length != 3){
			System.out.println("Usage: java Receiver <port number> <receiving timeout in ms> <expected packets> <-v for verbose>");
		}else{
			try{
				receive(args);
			}catch(IOException e){
				System.err.println("Socket error");
			}
		}
	}
	
	/**
	 * receives UDP packets. The content of the packets is saved in
	 * the buffer byte-array. Also counts and prints the amount of packets received.
	 * @param args cmd-arguments passed from main
	 * @throws IOException exception is forwarded to be handled by calling method
	 */
	private static void receive(String[] args)throws IOException{
        long beforeTime = 0;
        long afterTime = 0;
		//buffer to hold the received message represented as byte-array
		byte[] buffer = new byte[SIZE];
		//create socket with port-number specified in args[0].
		DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
		//set socket-timeout to terminate following loop, if no more packets received
		socket.setSoTimeout(Integer.parseInt(args[1]));
		//create datagram-packet with previously defined buffer. Received packets will be saved in this variable
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		//get expected amount  of packets
        int expec = Integer.parseInt(args[2]);

        CRC32 check = new CRC32();
		int packetCount = 0;
		try{
			//infinite loop to receive packets. Only terminated if no packets arrive for a certain specified time
			//e.g. if a connection problem occurs or no more packets are sent by the Sender.
			socket.receive(packet);
			check.update(packet.getData());
			beforeTime = System.currentTimeMillis();
			packetCount++;
			while(true){
				socket.receive(packet);
				afterTime = System.currentTimeMillis();
				packetCount++;
				byte[] message = packet.getData();
				if(packetCount < expec){
					check.update(message);
				}else{
					check.update(Arrays.copyOfRange(message, 0, message.length-4));
				}
				if(args[3] != null && args[3].equals("-v")){
					print(packet,packetCount,expec);
				}
			}
		//if the socket-timeout is reached the thrown SocketTimeoutException is caught here. Number of received
		//packets is printed.
		}catch(SocketTimeoutException e){}
		socket.close();
		System.out.println(check.getValue());
		evaluate(afterTime, beforeTime, packet.getLength()-5, packetCount);
	}
	
	
	/**
	 * evaluates the sending process. Computes Transfer-speed based on sent data and transfer-time
	 * @param after timestamp after sending
	 * @param before timestamp before sending
	 * @param size size of a package's Message
	 * @param amnt Amount of packets sent
	 */
	private static void evaluate(long after, long before, int size, int amnt){
		System.out.println(amnt + " packets received");
		long duration = after - before;
		int totalSize = size * amnt;
		double speed = (double)(totalSize)/(double)(duration);
		if(speed != Double.NaN && speed != Double.POSITIVE_INFINITY){
			System.out.println(String.format("%.2f KB/s", speed));
		}else{
			System.out.println("speed not calculatable");
		}
		
	}
	
	private static void print(DatagramPacket packet, int current, int expec){
		byte[] message = packet.getData();
		ByteBuffer buf = ByteBuffer.wrap(message);
		System.out.print(buf.getInt());
		if(current < expec){
			System.out.println(new String(message,4,message.length-4));
		}else{
			System.out.println(new String(message,4,message.length-8));
			//System.out.println(buf.getInt(buf.array().length-4));
		}
	}
	
	private static long getUnsigned(int checksum){
		if(checksum >= 0){
			return (long)checksum;
		}else{
			return (-Integer.MIN_VALUE) + (checksum - Integer.MIN_VALUE);
			
		}
		
	}
}







