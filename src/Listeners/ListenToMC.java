package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Files.ChunkInfo;
import Main.Peer;
import Message.Message;
import Utilities.Pair;

public class ListenToMC implements Runnable{
	@Override
	public void run() {
		try {
			Peer.mc_socket.joinGroup(Peer.mc_saddr.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			byte[] receiveData = new byte[64*1024];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			byte[] finalArray = null;
			try {
				Peer.mc_socket.receive(rp);
				finalArray = new byte[rp.getLength()];
				System.arraycopy(rp.getData(), 0, finalArray, 0, rp.getLength());
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				message = Message.fromByteArray(finalArray);
				
				if(message.type == Message.Type.STORED){
					this.filterStoredMessage(rp, message);
				} else if(message.type == Message.Type.REMOVED){
					Peer.removed_messages.add(message);
				} else if(!rp.getAddress().equals(InetAddress.getLocalHost())){/*IGNORE MESSAGES FROM ITSELF*/
					if(message.type == Message.Type.GETCHUNK){
						Peer.getchunk_messages.add(message);
					} else if(message.type == Message.Type.DELETE){
						Peer.delete_messages.add(message);
					} 
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
		}
	}
	
	private void filterStoredMessage(DatagramPacket rp, Message message){
		Pair<String, ChunkInfo> peer = new Pair<String, ChunkInfo>(rp.getAddress().toString(), new ChunkInfo(message.getFileID().toString(), message.chunkNo, 0, 0));
		if(Peer.peers.contains(peer)){
			System.out.println("RECEIVED A DUPLICATE STORED MESSAGE FROM " + peer.getfirst() + " " + peer.getsecond().getFileId().toString() + " " + peer.getsecond().getChunkNo());
		} else {
			System.out.println("RECEIVED STORED MESSAGE " + peer.getfirst() + " " + peer.getsecond().getFileId().toString() + " " + peer.getsecond().getChunkNo());
			Peer.peers.add(peer);
			/*
			 * UPDATE ACTUAL REPLICATION DEGREE OF THE CHUNK UPON VALID STORE MESSAGE
			 */
			Peer.updateActualRepDegree(message, 1);
		}
	}
	
}