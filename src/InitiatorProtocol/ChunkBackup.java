package InitiatorProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Main.Chunk;
import Main.Peer;
import Message.Message;


public class ChunkBackup {
	private int count = 0;
	private int timeInterval = 500;
	final Message msg;
	final DatagramPacket msgPacket;
	
	public ChunkBackup(Chunk chunk) throws IOException{
		msg = Message.makePutChunk(chunk);
		
		byte[] temp = msg.toByteArray();
		
		msgPacket = new DatagramPacket(temp,
										  temp.length,
										  Peer.mdb_saddr.getAddress(),
										  Peer.mdb_saddr.getPort());
		SendDelay sd = new SendDelay();
		sd.startTask(msgPacket, chunk, msg);
	}
	
	public class SendDelay{
		private Timer timer = new Timer();
		DatagramPacket p;
		Message message;
		Chunk chunk;
		
		public void startTask(DatagramPacket p, Chunk chunk, Message msg){
			this.p = p;
			this.chunk = chunk;
			this.message = msg;
			Random rand = new Random();
			timer.schedule(new PeriodicTask(), rand.nextInt(10));
		}
		
		private class PeriodicTask extends TimerTask{
			@Override
			public void run() {
				try {
					//System.out.println("SENDING MESSAGE");
					Peer.mdb_socket.send(p);
					TaskManager task = new TaskManager();
					task.startTask(message, chunk);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	public class TaskManager {
	    private Timer timer = new Timer();
	    Message msg;
	    Chunk chunk;
	    
	    public void startTask(Message msg, Chunk chunk) {
	    	this.msg = msg;
	    	this.chunk = chunk;
	        timer.schedule(new PeriodicTask(), timeInterval);
	    }

	    private class PeriodicTask extends TimerTask {
	        @Override
	        public void run(){
	        	count++;
	        	if(count == 5){
	        		System.out.println("GAME OVER MAN, GAME OVER!");
	        	}else{
	        		int numStored = Peer.getStoredMessages(chunk);
	        		if(numStored >= chunk.replicationDeg){
	        			System.out.println("RECEIVED CONFIRMATION OF STORED CHUNK NO " + chunk.chunkNo + " RD: " + chunk.replicationDeg);
	        			Peer.mutex_stored_messages.lock();
	        			Peer.removeStoredMessages(chunk);
	        			Peer.mutex_stored_messages.unlock();
	        		}else{
	        			try {
							Peer.mdb_socket.send(msgPacket);
						} catch (IOException e) {
							e.printStackTrace();
						}
	        			timeInterval*=2;
	        			timer.schedule(new PeriodicTask(), timeInterval);
	        		}
	        	}
	        }
	    }
	}
}