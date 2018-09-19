import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Waralee Tanaphantaruk		ID 5988044
 * Pattararat Kiattipadungkul	ID 5988068
 * Thanakorn Torcheewee			ID 5988148
 * Section 1
 */


public class Index {

	// Term id -> (position in index file, doc frequency) dictionary
	private static Map<Integer, Pair<Long, Integer>> postingDict
			= new TreeMap<Integer, Pair<Long, Integer>>();
	// Doc name -> doc id dictionary
	private static Map<String, Integer> docDict
			= new TreeMap<String, Integer>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict
			= new TreeMap<String, Integer>();
	// Block queue
	private static LinkedList<File> blockQueue
			= new LinkedList<File>();

	// Total file counter
	private static int totalFileCount = 0;
	// Document counter
	private static int docIdCounter = 0;
	// Term counter
	private static int wordIdCounter = 0;
	// Index
	private static BaseIndex index = null;

	private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
	/*
	 * Write a posting list to the given file
	 * You should record the file position of this posting list
	 * so that you can read it back during retrieval
	 *
	 * */
	private static void writePosting(FileChannel fc, PostingList posting)
			throws IOException {
		/*
		 * TODO: Your code here
		 *
		 */
		ByteBuffer buf = ByteBuffer.allocate(INT_BYTES*(posting.getList().size()+2));
		//Write term id of Postlist to ByteBuffer
		buf.putInt(posting.getTermId());
		//Write term frequency to ByteBuffer
		buf.putInt(posting.getList().size());
		//Write all docID to Postlist to ByteBuffer
		for (int docID : posting.getList()){
			buf.putInt(docID);
		}
		buf.flip();

		fc.write(buf);
	}


	/**
	 * Pop next element if there is one, otherwise return null
	 * @param iter an iterator that contains integers
	 * @return next element or null
	 */
	private static Integer popNextOrNull(Iterator<Integer> iter) {
		if (iter.hasNext()) {
			return iter.next();
		} else {
			return null;
		}
	}

	/**
	 * Main method to start the indexing process.
	 * @param method		:Indexing method. "Basic" by default, but extra credit will be given for those
	 * 			who can implement variable byte (VB) or Gamma index compression algorithm
	 * @param dataDirname	:relative path to the dataset root directory. E.g. "./datasets/small"
	 * @param outputDirname	:relative path to the output directory to store index. You must not assume
	 * 			that this directory exist. If it does, you must clear out the content before indexing.
	 */
	public static int runIndexer(String method, String dataDirname, String outputDirname) throws IOException
	{
		/* Get index */
		String className = method + "Index";
		try {
			Class<?> indexClass = Class.forName(className);
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get root directory */
		File rootdir = new File(dataDirname);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + dataDirname);
			return -1;
		}


		/* Get output directory*/
		File outdir = new File(outputDirname);
		if (outdir.exists() && !outdir.isDirectory()) {
			System.err.println("Invalid output directory: " + outputDirname);
			return -1;
		}

		/*	TODO: delete all the files/sub folder under outdir
		 *
		 */

		try {
			//Deleting the directory recursively.
			//Call delete function to delete all children in directory
			delete(outdir);
//			System.out.println("Directory has been deleted recursively !");
		} catch (IOException e) {
			System.out.println("Problem occurs when deleting the directory : " + outputDirname);
			e.printStackTrace();
		}


		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				System.err.println("Create output directory failure");
				return -1;
			}
		}


		/* BSBI indexing algorithm */
		File[] dirlist = rootdir.listFiles();

		/* For each block */
		for (File block : dirlist) {
			File blockFile = new File(outputDirname, block.getName());
			blockQueue.add(blockFile);

			File blockDir = new File(dataDirname, block.getName());
			File[] filelist = blockDir.listFiles();

			//posting is a variable to store all Term PostingList in one block
			Map<Integer, ArrayList<Integer>> posting = new TreeMap<Integer, ArrayList<Integer>>();

			/* For each file */
			for (File file : filelist) {
				++totalFileCount;
				String fileName = block.getName() + "/" + file.getName();
				// use pre-increment to ensure docID > 0
				int docId = ++docIdCounter;
				docDict.put(fileName, docId);


				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						/*
						 * TODO: Your code here
						 *       For each term, build up a list of
						 *       documents in which the term occurs
						 */
						
						//Check id token is already existed in termDict. If yes, then add docID to existing postinglist
						//If no, then add assign new termID and add new term to termDict
						if(termDict.containsKey(token) == false){
							int termId = ++wordIdCounter;
							termDict.put(token, termId);
							ArrayList<Integer> tempList = new ArrayList<>();
							posting.put(termId, tempList);
							posting.get(termId).add(docId);
						}else{
							int tempId = termDict.get(token);
							if(posting.get(tempId)!=null ){
								if(!posting.get(tempId).contains(docId)){
									posting.get(tempId).add(docId);
								}
							}
							else{
								ArrayList<Integer> tempList = new ArrayList<>();
								posting.put(tempId, tempList);
								posting.get(tempId).add(docId);
							}
						}
					}
				}
				reader.close();
			}

			/* Sort and output */
			if (!blockFile.createNewFile()) {
				System.err.println("Create new block failure.");
				return -1;
			}

			RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");
			/*
			 * TODO: Your code here
			 *       Write all posting lists for all terms to file (bfc)
			 */

			for(int keyId : posting.keySet()){
				writePosting(bfc.getChannel(), new PostingList(keyId, posting.get(keyId)));
			}
			bfc.close();
		}

		/* Required: output total number of files. */
		System.out.println("Total Files Indexed: "+totalFileCount);

		/* Merge blocks */
		while (true) {
			if (blockQueue.size() <= 1)
				break;

			File b1 = blockQueue.removeFirst();
			File b2 = blockQueue.removeFirst();
			
			System.out.println(b1.getName());
			System.out.println(b2.getName());
			
			File combfile = new File(outputDirname, b1.getName() + "+" + b2.getName());
			if (!combfile.createNewFile()) {
				System.err.println("Create new block failure.");
				return -1;
			}

			RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
			RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
			RandomAccessFile mf = new RandomAccessFile(combfile, "rw");

			/*
			 * TODO: Your code here
			 *       Combine blocks bf1 and bf2 into our combined file, mf
			 *       You will want to consider in what order to merge
			 *       the two blocks (based on term ID, perhaps?).
			 *
			 */
			// create an array equal to the length of raf
			// Call mergeFile method
			try {
				mergeFile(bf1, bf2, mf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			bf1.close();
			bf2.close();
			mf.close();
			b1.delete();
			b2.delete();
			blockQueue.add(combfile);
		}

		/* Dump constructed index back into file system */
		File indexFile = blockQueue.removeFirst();
		indexFile.renameTo(new File(outputDirname, "corpus.index"));

		// modify postingDict( Map<Integer, Pair<Long, Integer>> postingDict)
		// by reading corpus.index
        RandomAccessFile indexf = new RandomAccessFile(new File(outputDirname+"/corpus.index"), "r");
        
        //open file channel
        int i = 0; // index of each numbers
        FileChannel fc = indexf.getChannel();
        IntBuffer indexbuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).asIntBuffer();
        try
        {
            while(true)
            {
				int termid = indexbuffer.get(i);
				int pos = i;
				i++;
				int docfreq = indexbuffer.get(i);
				i++;
				Pair tempPair = new Pair<Long,Integer>((long) (pos*4), docfreq);
				System.out.println(termid+"  "+(pos*4)+"  "+docfreq);
				i+=docfreq;
				postingDict.put(termid, tempPair);
            }
        }
        catch(Exception e)
        {
            System.out.println("reading done\n");
        }
			

		BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "term.dict")));
		for (String term : termDict.keySet()) {
			termWriter.write(term + "\t" + termDict.get(term) + "\n");
		}
		termWriter.close();

		BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "doc.dict")));
		for (String doc : docDict.keySet()) {
			docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
		}
		docWriter.close();

		BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
				outputDirname, "posting.dict")));
		for (Integer termId : postingDict.keySet()) {
			postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
					+ "\t" + postingDict.get(termId).getSecond() + "\n");
		}
		postWriter.close();

		return totalFileCount;
	}
	
	/**
	 * This method is for merging two block of data and output as one single block
	 * @param bf1 First block
	 * @param bf2 Second block
	 * @param mf output file after merge
	 * @throws Exception
	 */

	private static void mergeFile(RandomAccessFile bf1, RandomAccessFile bf2, RandomAccessFile mf) throws Exception{
		/*
		step 1: create file channel from bf1, bf2, mf
		step 2: read file from bf1 and bf2
		step 3: read term id and posting list length from bf1 and bf2
		step 4: if term id 1 = term id 2 then merge posting list
				else if term id 1 < term id 2
					put all posting list of term id 1 to merged file then go to next term
				else
					put all posting list of term id 2 to merged file then go to next term
		step 5: if bf1 ended
					put the rest (ที่เหลือ) of bf2 to mf
				else
					put the rest of bf1 to mf
		 */
		FileChannel ch1 = bf1.getChannel();
		FileChannel ch2 = bf2.getChannel();
		FileChannel mfchannel = mf.getChannel();
		
		
		ByteBuffer buf = ByteBuffer.allocate((int) (INT_BYTES*(ch1.size()+ch2.size())));
		//open file channel
        int i = 0; // index of each number
        int j = 0;
        IntBuffer ib = ch1.map(FileChannel.MapMode.READ_ONLY, 0, ch1.size()).asIntBuffer();
        IntBuffer ib2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0, ch2.size()).asIntBuffer();
        try
        {	//keep merging until first block or second block end
            while((i*4) < ch1.size()&&(j*4)<ch2.size())
            {	
				int termid = ib.get(i);
				int termid2 = ib2.get(j);
				int docfreq = ib.get(i+1);
				int docfreq2 = ib2.get(j+1);

				//If termID is equal, combine into one and put into 
				if(termid == termid2){
					buf.putInt(termid);		//Add term ID to buffer
					i+=2;
					j+=2;
					int limit1 = i+docfreq;
					int limit2 = j+docfreq2;
					int totalFreq = docfreq+docfreq2;
					buf.putInt(totalFreq);	//Add docFreq to buffer
					
					//Merge Doc ID 
					while(i<limit1&&j<limit2){
						if(ib.get(i) < ib2.get(j)){
							buf.putInt(ib.get(i));	//Add docID from block1
							i++;
						}
						else if(ib.get(i) > ib2.get(j)){
							buf.putInt(ib2.get(j));	//Add docID from block 2
							j++;
						}
						else{ //equal
							buf.putInt(ib.get(i));	//Add docID
							i++;
							j++;
						}
					}
					while(i<limit1){
						//Add remain docID 
						buf.putInt(ib.get(i));
						i++;
					}
					while(j<limit2){
						//Add remain docID 
						buf.putInt(ib2.get(j));
						j++;
					}
				}
				else if(termid < termid2){ //If term ID of the of first block is greater than second block add term of first block
					buf.putInt(termid);
					i++;
					buf.putInt(docfreq);
					i++;
					int len = i+docfreq;
					while(i<len)
					{	
						buf.putInt(ib.get(i));
						i++;
					}
					
				}
				else{ //termid < termid2
					buf.putInt(termid2);
					j++;
					buf.putInt(docfreq2);
					j++;
					int len = j+docfreq2;
					while(j<len)
					{	
						buf.putInt(ib2.get(j));
						j++;
					}
				}
            }
            
            //Add remain data in file
            while(i < (ch1.size()/4)){
            	buf.putInt(ib.get(i));
				i++;
            }
            while(j < (ch2.size()/4)){
            	buf.putInt(ib2.get(j));
				j++;
            }
        }
        catch(Exception e)
        {	
            System.out.println("reading done\n");
        }
        buf.flip();
        mfchannel.write(buf);

	}
	/**
	 * This function delete children in directory recursively
	 * @param file
	 * @throws IOException
	 */
	private static void delete(File file) throws IOException {

		for (File childFile : file.listFiles()) {

			if (childFile.isDirectory()) {
				delete(childFile);
			} else {
				if (!childFile.delete()) {
					throw new IOException();
				}
			}
		}

		if (!file.delete()) {
			throw new IOException();
		}
	}
	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 3) {
			System.err
					.println("Usage: java Index [Basic|VB|Gamma] data_dir output_dir");
			return;
		}

		/* Get index */
		String className = "";
		try {
			className = args[0];
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get root directory */
		String root = args[1];


		/* Get output directory */
		String output = args[2];
		runIndexer(className, root, output);
	}

}