import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class testanything
{
    //static File file = new File("./index/small/2");

//	public static void writeToFileChannel() throws Exception
//	{
//		RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//		//open file channel
//		FileChannel fc = raf.getChannel();
//
//		//String value = "12319898651324";
//		//System.out.println(value.length());
//	    //byte[] strBytes = value.getBytes();
//		int num = 555;
//		int num2 = 123;
//
//		// buffer size = number of int * 4
//		int bcount = 8; // 2*4 bytes
//		//fc.map(mapmode, where to start, number of bytes)
//		IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), bcount).asIntBuffer();
//		ib.put(num);
//		ib.put(num2);
//		/*ib.put(100);
//		ib.put(3);
//		ib.put(2);
//		ib.put(24);*/
//
//		fc.close();
//		raf.close();
//		System.out.println("write complete");
//	}

    public static void readFileChannel(String filename) throws Exception
    {
        File file = new File(filename);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        
        //open file channel
        int countbyte = 0;
        int i = 0; // index of each number
        int len;
        FileChannel fc = raf.getChannel();
        IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size()).asIntBuffer();
        System.out.println("Size: "+fc.size());
        try
        {
            while(true)
            {
                //System.out.println("byte " + countbyte + ": " + ib.get(i));
                //countbyte+=4;
               // System.out.println(ib.get(i));
               // i++;
				int termid = ib.get(i);
				i++;
				int docfreq = ib.get(i);
				i++;
				System.out.println("term id = " + termid + " doclen = " + docfreq);
				System.out.print("posting list: ");
				for(len = i; len < i+docfreq; len++)
				{
					System.out.print(ib.get(len) + " ");
				}
				System.out.println("\n");
				i+=docfreq;
            }
        }
        catch(Exception e)
        {
            System.out.println("reading done\n");
            //System.out.println(i);
            //System.out.println(fc.size()/4);
        }
    }

    public static void main(String[] args) throws Exception
    {
        // TODO Auto-generated method stub
        //writeToFileChannel();
        //writeToFileChannel();
        //readFileChannel("./index/small - Copy/corpus.index");
//        readFileChannel("./index/small/corpus.index");
        //readFileChannel("./index/small/1");
    	readFileChannel("./output/small/2.out");
    }

}