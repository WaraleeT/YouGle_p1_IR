import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class BasicIndex implements BaseIndex {

	@Override
	public PostingList readPosting(FileChannel fc) {
		/*
		 * TODO: Your code here
		 *       Read and return the postings list from the given file.
		 */
		try {
			ByteBuffer buffer = ByteBuffer.allocate(1024); 
			// reading data from file channel into buffer 
//			int numOfBytesRead = fc.read(buffer); 
//			System.out.println("number of bytes read : " + numOfBytesRead);
			int bytesRead = fc.read(buffer);
            while (bytesRead != -1) {
                buffer.flip();

                while (buffer.hasRemaining()) {
                    System.out.print((int) buffer.get());
                }

                buffer.clear();
                bytesRead = fc.read(buffer);
            }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) {
		/*
		 * TODO: Your code here
		 *       Write the given postings list to the given file.
		 */
		
	}
}

