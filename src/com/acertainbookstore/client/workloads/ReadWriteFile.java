package com.acertainbookstore.client.workloads;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ReadWriteFile {
	private static String file = "data.txt";
	private static String rfile = "result.txt";
	public static void Write(String con) throws IOException{
		FileWriter fw = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(fw);
		pw.printf("%s"+"%n", con);
		pw.close();
	}
	
	public static void WriteResult(String con) throws IOException{
		FileWriter fw = new FileWriter(rfile, true);
		PrintWriter pw = new PrintWriter(fw);
		pw.printf("%s"+"%n", con);
		pw.close();
	}
	
	public static void Read()
	{
		float totalL = 0;
		float totalT = 0;
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(file));
	      try {
	        String line = null; //not declared within while loop

	        while (( line = input.readLine()) != null){
	          String[] l = line.split("-");
	          totalL += Float.parseFloat(l[0]);
	          totalT += Float.parseFloat(l[1]);
	        }
	      }
	      finally {
	        input.close();
	        WriteResult(totalL/10 + "-" +totalT/10);
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	}
	
	public static void main(String[] args)
	{
		Read();
	}
}
