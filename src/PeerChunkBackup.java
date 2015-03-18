import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class PeerChunkBackup {
	
	private Message msg;
	private Timer timer;
	
	PeerChunkBackup(Message msg){
		this.msg = msg;
		this.timer = new Timer();
		Random rand = new Random();
		this.timer.schedule(new Task(), rand.nextInt(401));
	}
	
	private class Task extends TimerTask{
		@Override
		public void run(){
			Peer.writeChunk(msg);
			Message storedMsg = new Message(Message.Type.STORED);
			storedMsg.setFileID(msg.getFileID());
			storedMsg.setVersion(1, 0);
			storedMsg.setChunkNo(msg.getChunkNo());
			DatagramPacket storedPacket = new DatagramPacket(storedMsg.toByteArray(),
												storedMsg.toByteArray().length,
												Peer.mc_saddr.getAddress(),
												Peer.mc_saddr.getPort());
			try {
				System.out.println("ANSWERING WITH STORED " + msg.getChunkNo());
				Peer.mc_socket.send(storedPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
