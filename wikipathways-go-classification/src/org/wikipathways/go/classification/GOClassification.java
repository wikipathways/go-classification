package org.wikipathways.go.classification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysClient;

public class GOClassification {
	
	private static WikiPathwaysClient client;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			long startTime = System.currentTimeMillis();
			// set up BridgeDb connection 
			// TODO: remove hard link to mapping db
			Class.forName("org.bridgedb.rdb.IDMapperRdb");
			IDMapper mapper = BridgeDb.connect("idmapper-pgdb:resources/Hs_Derby_20120602.bridge");
			
			new File("input").mkdir();
			new File("output").mkdir();
			new File("denom").mkdir();

			// write denominator file 
			// contains all Ensembl identifiers in the BridgeDb database
			Logger.log.info("Creating denominator file.");
			File denom = new File("denom/denominator.txt");
			InputFileCreator.createDenominatorFile(mapper, denom);
			
			client = new WikiPathwaysClient(new URL("http://www.wikipathways.org/wpi/webservice/webservice.php"));
			
			// list all human pathways and run GO analysis
			// TODO: allow user to specify species
			WSPathwayInfo [] pathways = client.listPathways(Organism.HomoSapiens);
			for(WSPathwayInfo p : pathways) {
				runGoElite(p.getId(), mapper, denom, p.getName());
			}
			
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    int minutes = (int) ((elapsedTime / 1000) / 60);
		    System.out.println("\nAnalysis duration: " + minutes + " minutes.");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IDMapperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private static void runGoElite(String pathwayId, IDMapper mapper, File denom, String pathwayName) throws IOException {
		
		Logger.log.info("Write input file for pathway " + pathwayId + ".");
		File input = new File("input/input_" + pathwayId + ".txt");
		int geneCount = InputFileCreator.createInputFile(mapper, input, pathwayId, client);
		
		Logger.log.info("Pathway " + pathwayId + " contains " + geneCount + " genes.");
		if(geneCount > 10) {
			File output = new File("output/output_" + pathwayId + ".txt");
			WebService webService = new WebService();
			URL[] vec = webService.launchJob(denom, input);
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			if(vec != null) {
				writer.write("# " + input.getName() + "\t" + pathwayName + "\t" + geneCount + "\n");
				writer.write("term\tid\tchanged\tmeasured\ttotal\tzscore\tweighted zscore\tredundant with\n");

				boolean status = WebServiceConnector.catchData(vec, input.getName(), writer);
				
				if (status){ 
					Logger.log.info("Pathway " + pathwayId + ": analysis finished.");
				} 
			} else {
				Logger.log.warn("Pathway " + pathwayId + ": no results found.");
				writer.write("No result");
			}
			writer.close();
		} else {
			Logger.log.warn("Pathway " + pathwayId + " is too small to perform GO over representation analysis.");
		}
	}

}
