package org.wikipathways.go.classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class WebServiceConnector {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File denom = new File("denominator/denominator.txt");
		
		for(File input : new File("input/").listFiles()) {
			File output = new File("results/" + input.getName());
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
//			File input = new File("/Users/martinakutmon/sf-2013/go-elite/input/input_Hs_ACE_Inhibitor_Pathway_WP554_4824.txt");
			WebService webService = new WebService();
			URL[] vec = webService.launchJob(denom, input);
			if(vec != null) {
				writer.write("Term\tId\tChanged\tMeasured\ttotal\tzscore\tweighted zscore\tredundant with\n");

				boolean status = catchData(vec, input.getName(), writer);
				
				if (status){ 
					System.out.println("DONE");
				} // if data is cached process data 
			} else {
				writer.write("No result");
				System.out.println("ERROR");
			}
			writer.close();
		}
	}
	private static URL[] resultURL;
	private static URL loopURL;
	private static String nameType;
	private static File resultsFile;
	
	private static boolean status = false;
	public static boolean catchData(URL[] resultVec, String inputFileName, BufferedWriter writer) throws IOException{
		int count = 0;
		status = false;
		String [] returnTypes = {"RESULT_FULL_GO",	"RESULT_FULL_PATHWAY", "RESULT_PRUNED_GO_AND_PATHWAY",
				"RESULT_STDOUT","RESULT_STDERR","RESULT_LOG"};	
		
		resultURL = resultVec;
		
		for (URL url : resultURL){                                // writing all results to a file by looping 
			loopURL = url;
			for (String type : returnTypes){                                                     // using type as name value for ..txt
				URL check = resultURL[ WebService.ReturnTypes.valueOf(type).ordinal()];
				nameType = type;
				Vector<String> content = null;
				
				if (loopURL != null && loopURL == check){
					content = Utilities.getFileContents(loopURL);
					
					
					resultsFile = new File(nameType+".txt");  // temp files should be erased when plugin is finished
					if (nameType.equalsIgnoreCase("RESULT_PRUNED_GO_AND_PATHWAY")){
						PrintWriter outprunned = new PrintWriter(new FileWriter(resultsFile)); // no such file / directory
						count ++;
						for (Enumeration<String> inhoud = content.elements(); inhoud.hasMoreElements(); )
						       outprunned.println(inhoud.nextElement()); 
						
						outprunned.close();
						
						BufferedReader reader = new BufferedReader(new FileReader(resultsFile));

						String line = null;
						reader.readLine();

						while ((line = reader.readLine()) != null) {

							// reading line by line, /t seperated
							// input_list-GO.txt GO Type GO Name(GOID) Number Changed Number
							// Measured Number in GO Percent Changed Percent Present Z Score
							// PermuteP AdjustedP redundant with terms inverse redundant
							// gene symbols

							String[] str = line.split("\t");
							if (str.length > 7) {
								if(str[1].contains("biological_process")) {
									String[] splitGoTeId = str[2].split("[()]");
									String goTerm = splitGoTeId[0];
									String goId = splitGoTeId[1];
									Integer numChanged = Integer.valueOf(str[3]);
									Integer numMeasured = Integer.valueOf(str[4]);
									Integer numGo = Integer.valueOf(str[5]);
									Double zScore = Double.valueOf(str[8]);
									writer.write(goTerm + "\t" + goId + "\t" + numChanged + "\t" + numMeasured
											+ "\t" + numGo + "\t" + zScore + "\t" + ((Math.log(numChanged)/Math.log(2)) * zScore) + "\t" + str[11] +"\n");
								}
							}
						}
						reader.close();
						resultsFile.delete();
					}
				}
				if (count > 2) { status = true;}
			}
		}		
		return status;
	}	

}
