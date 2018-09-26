/*Waralee Tanaphantaruk 5988044 sec 1
Pattararat Kiatpadungkul 5988068 sec1
Thanahorn Torcheewee 5988148 sec1*/
import java.awt.List;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.swing.plaf.synth.SynthSplitPaneUI;



public class BasicIndex implements BaseIndex {

	private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;

	@Override
	public PostingList readPosting(FileChannel fc) {
		/*
		 * TODO: Your code here
		 *       Read and return the postings list from the given file.
		 */	
		int termId = 0;
		int freq = 0;
		ArrayList<Integer> docList = new ArrayList<Integer>();
		ByteBuffer buf = ByteBuffer.allocate(INT_BYTES);
		int count = 0;
		try {
			if(fc.read(buf)==-1){
//				System.out.println("End");
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//read term ID
		buf.flip();
		termId = buf.getInt();
		buf.clear();
		
		//read again for doc freq
		try {
			fc.read(buf);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buf.flip();
		freq = buf.getInt();
		buf.clear();
		
//		System.out.println("Read term ID: "+termId+" f: "+freq);
		count = 0;
		for (int i = 0; i < freq;i++){
			try {
				fc.read(buf);
			} catch (Exception e) {
				System.out.println("Read error");
			}
			buf.flip();
			docList.add(buf.getInt());
			buf.clear();
		}
		
//		System.out.println(docList);
		PostingList posting = new PostingList(termId, docList);
		return posting;		
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) {
		/*
		 * TODO: Your code here
		 *       Write the given postings list to the given file.
		 */
		ByteBuffer buf = ByteBuffer.allocate(INT_BYTES*(p.getList().size()+2));
		//Write term id of Postlist to ByteBuffer
		buf.putInt(p.getTermId());
		//Write term frequency to ByteBuffer
		buf.putInt(p.getList().size());
		//Write all docID to Postlist to ByteBuffer
		for (int docID : p.getList()){
			buf.putInt(docID);
		}
		buf.flip();

		try {
			fc.write(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

