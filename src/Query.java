

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.print.attribute.Size2DSyntax;

public class Query {

	// Term id -> position in index file
	private  Map<Integer, Long> posDict = new TreeMap<Integer, Long>();
	// Term id -> document frequency
	private  Map<Integer, Integer> freqDict = new TreeMap<Integer, Integer>();
	// Doc id -> doc name dictionary
	private  Map<Integer, String> docDict = new TreeMap<Integer, String>();
	// Term -> term id dictionary
	private  Map<String, Integer> termDict = new TreeMap<String, Integer>();
	// Index
	private  BaseIndex index = null;
	
	
	//indicate whether the query service is running or not
	private boolean running = false;
	private RandomAccessFile indexFile = null;
	
	/* 
	 * Read a posting list with a given termID from the file 
	 * You should seek to the file position of this specific
	 * posting list and read it back.
	 * */
	private  PostingList readPosting(FileChannel fc, int termId)
			throws IOException {
		/*
		 * TODO: Your code here
		 */
		//Given that fc is an corpus.index
		Long pos = posDict.get(termId);
//		System.out.println("term "+termId);
		int posInt = (int)(pos/4);
		int freq = freqDict.get(termId);
		posInt += 2; //Skip doc freq and termId		
        IntBuffer ib = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).asIntBuffer();
        ArrayList<Integer> docID = new ArrayList<>();
        try
        {	
            ib.get(posInt);
			for(int i = posInt; i < (posInt + freq); i++)
			{
				docID.add(ib.get(i));
//				System.out.print(ib.get(i)+",");
			}
            
        }
        catch(Exception e)
        {
            System.out.println("reading done\n");
            //System.out.println(i);
            //System.out.println(fc.size()/4);
        }
		PostingList result = new PostingList(termId, docID);
		
		return result;
	}
	
	
	public void runQueryService(String indexMode, String indexDirname) throws IOException
	{
		//Get the index reader
		try {
			Class<?> indexClass = Class.forName(indexMode+"Index");
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}
		
		//Get Index file
		File inputdir = new File(indexDirname);
		if (!inputdir.exists() || !inputdir.isDirectory()) {
			System.err.println("Invalid index directory: " + indexDirname);
			return;
		}
		
		/* Index file */
		indexFile = new RandomAccessFile(new File(indexDirname,
				"corpus.index"), "r");

		String line = null;
		/* Term dictionary */
		BufferedReader termReader = new BufferedReader(new FileReader(new File(
				indexDirname, "term.dict")));
		while ((line = termReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			termDict.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		termReader.close();

		/* Doc dictionary */
		BufferedReader docReader = new BufferedReader(new FileReader(new File(
				indexDirname, "doc.dict")));
		while ((line = docReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			docDict.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		docReader.close();

		/* Posting dictionary */
		BufferedReader postReader = new BufferedReader(new FileReader(new File(
				indexDirname, "posting.dict")));
		while ((line = postReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			posDict.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
			freqDict.put(Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[2]));
		}
		postReader.close();
		
		this.running = true;
	}
    
	public List<Integer> intersection(List<Integer> list1, List<Integer> list2){
		ArrayList<Integer> list = new ArrayList<Integer>();

        for (int t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
	}
	
	public List<Integer> retrieve(String query) throws IOException
	{	if(!running) 
		{
			System.err.println("Error: Query service must be initiated");
		}
		
		/*
		 * TODO: Your code here
		 *       Perform query processing with the inverted index.
		 *       return the list of IDs of the documents that match the query
		 *      
		 */
		
		String [] querySet = query.split(" ");
		ArrayList <PostingList> result = new ArrayList<>();
		
		
		for(String q : querySet){
//			System.out.println("Find "+q+" : ");
			if(termDict.get(q) != null){
				readPosting(indexFile.getChannel(), termDict.get(q)).getList();
				result.add(readPosting(indexFile.getChannel(), termDict.get(q)));
			}
			else{
				System.out.println("Not found");
			}
		}
		
		//Intersection
		if(result.size() == 1){ //One term
//			for(int i : result.get(0).getList()){
//				System.out.print(i+", ");
//			}
//			System.out.println();
			return result.get(0).getList();
		}
		else if(result.size()>1){
			
			List <Integer> temp = result.get(0).getList();
			int term = 1;
			while(term<result.size()){
				temp = intersection(temp, result.get(term).getList());
				term++;
			}
//			for(int i : temp){
//				System.out.print(i+", ");
//			}
//			System.out.println();
			return temp;
		}
		else{//Null
			return null;
		}
//		return null;
		
	}
	
	
    String outputQueryResult(List<Integer> res) {
        /*
         * TODO: 
         * 
         * Take the list of documents ID and prepare the search results, sorted by lexicon order. 
         * 
         * E.g.
         * 	0/fine.txt
		 *	0/hello.txt
		 *	1/bye.txt
		 *	2/fine.txt
		 *	2/hello.txt
		 *
		 * If there no matched document, output:
		 * 
		 * no results found
		 * 
         * */
    	String temp = "";
    	if(res != null){
    		for(int i : res){
    			System.out.println(docDict.get(i));
    			temp += docDict.get(i);
    		}
    	}
    	else{
    		temp = "no results found";
    	}
    	
    	return temp;
    }
	
	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 2) {
			System.err.println("Usage: java Query [Basic|VB|Gamma] index_dir");
			return;
		}

		/* Get index */
		String className = null;
		try {
			className = args[0];
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get index directory */
		String input = args[1];
		
		Query queryService = new Query();
		queryService.runQueryService(className, input);
		
		/* Processing queries */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/* For each query */
		String line = null;
		while ((line = br.readLine()) != null) {
			List<Integer> hitDocs = queryService.retrieve(line);
			queryService.outputQueryResult(hitDocs);
		}
		
		br.close();
	}
	
	protected void finalize()
	{
		try {
			if(indexFile != null)indexFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

