
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class P1Tester {
	
	public static String[] queriesSmall = {
			"hello",
			"bye",
			"you",
			"how are you",
			"how are you ?"
	};
	
	public static String[] queriesLarge ={
		"we are",
		"stanford class",
		"stanford students",
		"very cool",
		"the",
		"a",
		"the the",
		"stanford computer science"
	};
	
	public static String[] queriesCiteseer ={
			"shortest path algorithm",
			"support vector machine",
			"random forest",
			"convolutional neural networks",
			"jesus",
			"mahidol",
			"chulalongkorn",
			"thailand",
			"polar bears penguins tigers",
			"algorithm search engine",
			"innovative product design social media",
			"suppawong",
			"tuarob",
			"suppawong tuarob",
			"suppawong tuarob conrad tucker"
		};
	
	public static void testIndex(String indexMode, String dataDirname, String indexDirname)
	{
		StringBuilder str = new StringBuilder();
		int numFiles = -1;
		str.append("Indexing Test Result: "+indexDirname+":\n");
		long memoryBefore = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		long startTime = System.currentTimeMillis(); 
		try {
			numFiles = Index.runIndexer(indexMode, dataDirname, indexDirname);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long memoryAfter = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		long endTime = System.currentTimeMillis();
		File indexFile = new File(indexDirname, "corpus.index");
		long indexSize = indexFile.length();
		str.append("\tTotal Files Indexed: "+numFiles+"\n");
		str.append("\tMemory Used: "+((memoryAfter - memoryBefore)/1000000.0)+" MBs\n");
		str.append("\tTime Used: "+((endTime - startTime)/1000.0)+" secs\n");
		str.append("\tIndex Size: "+(indexSize/1048576.0)+" MBs\n");
		str.append("\tAlright. Good Bye.\n");
		
		System.out.println(str.toString());
		
		//Writing out the stats to a log file
		try {
			File file = new File(indexDirname, "stats.txt");

			// if file does not exist, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
		
			bw.write(str.toString());
			bw.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	public static void testQuery(String indexMode, String indexDirname, String[] queries, String outputDir)
	{	
		StringBuilder str = new StringBuilder();
		str.append("Query Test Result: "+Arrays.toString(queries)+":\n");
		
		long memoryBefore = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		long startTime = System.currentTimeMillis(); 
		
		Query queryService = new Query();
		try {
			queryService.runQueryService(indexMode, indexDirname);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File f = new File(outputDir);
		if(!f.exists()) f.mkdirs();
		
		for(int i = 0; i < queries.length; i++)
		{
			System.out.println("Query["+(i+1)+"]:"+queries[i]);
			List<Integer> hitDocs = null;
			try {
				hitDocs = queryService.retrieve(queries[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String output = queryService.outputQueryResult(hitDocs);
			try {
				File file = new File(outputDir, (i+1)+".out");
	
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
			
				bw.write(output);
				bw.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		long memoryAfter = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
		long endTime = System.currentTimeMillis();
		str.append("\tMemory Used: "+((memoryAfter - memoryBefore)/1000000.0)+" MBs\n");
		str.append("\tTime Used: "+((endTime - startTime)/1000.0)+" secs\n");
		str.append("\tNo problem. Have a good day.\n");
		System.out.println(str.toString());
		
		//Writing out the stats to a log file
		try {
			File file = new File(outputDir, "stats.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
					
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
				
			bw.write(str.toString());
			bw.close();
		} catch (IOException e) {
					
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void main(String [] args)
	{
		//Test the "small" dataset
		testIndex("Basic", "./datasets/small", "./index/small");
		testQuery("Basic", "./index/small", queriesSmall, "./output/small");
		
		//Test the "large" dataset
		//testIndex("Basic", "./datasets/large", "./index/large");
		//testQuery("Basic", "./index/large", queriesLarge, "./output/large");
		
		//Test the "citeseer" dataset
		//testIndex("Basic", "./datasets/citeseer", "./index/citeseer");
		//testQuery("Basic", "./index/citeseer", queriesCiteseer, "./output/citeseer");
	}
}
