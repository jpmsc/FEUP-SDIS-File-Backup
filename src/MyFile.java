import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


public class MyFile {
	
	private File myFile;
	private String absPath;
	private long lastModification;
	private long fileSize;
	private FileID fileID;
	
	private RandomAccessFile raf;
	
	public MyFile(String path) throws IOException{
		myFile = new File(path);
		absPath = myFile.getAbsolutePath();
	
		Path p = FileSystems.getDefault().getPath(absPath);
		
		BasicFileAttributes fileAttr = Files.readAttributes(p, BasicFileAttributes.class);
	
		lastModification = fileAttr.lastModifiedTime().toMillis();
		fileSize = fileAttr.size();
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String codifier = this.absPath + this.lastModification + new Date().getTime();
			this.setFileID(new FileID(digest.digest(codifier.getBytes(StandardCharsets.UTF_8))));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FileID getFileID() {
		return fileID;
	}

	public void setFileID(FileID fileID) {
		this.fileID = fileID;
	}
	
	public long getFileSize(){
		return this.fileSize;
	}
	
	public String getPath(){
		return this.absPath;
	}
	
	public int getNumberofChunks(){
		return (int)(getFileSize() / Chunk.CHUNK_MAX_SIZE) + 1;
	}
	
	public byte[] getChunk(int chunkNo) throws IOException{
		long chunkPos = chunkNo * Chunk.CHUNK_MAX_SIZE;
		long arraySize = Math.min(Chunk.CHUNK_MAX_SIZE, this.fileSize - chunkPos);
		if(chunkPos > this.fileSize || raf == null){
			return new byte[0];
		}
		
		byte[] result = new byte[(int) arraySize];
		synchronized(raf){
			raf.seek(chunkPos);
			raf.read(result);
		}
		
		return result == null? new byte[0] : result;
	}
	
	public void open() throws FileNotFoundException{
		if(this.raf == null)
			raf = new RandomAccessFile(this.myFile, "r");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
