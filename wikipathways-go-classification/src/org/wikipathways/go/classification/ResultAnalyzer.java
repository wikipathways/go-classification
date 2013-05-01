package org.wikipathways.go.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResultAnalyzer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/Users/martinakutmon/sf-2013/go-elite/results/");
		
		int [] counts = new int [55];
		int [] countsSmall = new int [55];
		for(File file : dir.listFiles()) {
			if(file.getName().endsWith(".txt")) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				
				String line;
				reader.readLine();
				int count = 0;
				int countSmall = 0;
				while((line = reader.readLine()) != null) {
					String [] buffer = line.split("\t");
					Integer changed = Integer.parseInt(buffer[2]);
					Integer measured = Integer.parseInt(buffer[3]);
					if(changed > 2) {
						count++;
						if(measured < 2000) {
							countSmall++;
						}
	//					System.out.println(line);
					}
				}
				counts[count]++;
				countsSmall[countSmall]++;
				System.out.println(file.getName() + "\t" + count + "\t" + countSmall);
				reader.close();
			}
		}
		int sum = 0;
		int num = 0;
		int sumSmall = 0;
		for(int i = 0; i < counts.length; i++) {
			sum = sum + (i * counts[i]);
			num = num + counts[i];
			sumSmall = sumSmall + (i * countsSmall[i]);
			System.out.println(i + "\t" + counts[i] + "\t" + countsSmall[i]);
		}
		System.out.println(sum/num);
		System.out.println(sumSmall/num);

	}

}
