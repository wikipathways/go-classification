// GoElitePlugin for PathVisio,
// plugin to connect to GoElite webservice
// Copyright 2013 Department of Bioinformatics - BiGCaT, Maastricht University
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.wikipathways.go.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.xml.rpc.ServiceException;

import edu.sdsc.nbcr.opal.AppServiceLocator;
import edu.sdsc.nbcr.opal.AppServicePortType;
import edu.sdsc.nbcr.opal.types.FaultType;
import edu.sdsc.nbcr.opal.types.InputFileType;
import edu.sdsc.nbcr.opal.types.JobInputType;
import edu.sdsc.nbcr.opal.types.JobSubOutputType;
import edu.sdsc.nbcr.opal.types.StatusOutputType;

/** 
 * TODO: documentation
 */
public class WebService {

//	private File denomFile;
//	private File inputFile;
//	
	// arguments to expose for launchJob()
	public static String ARG_GENELIST_FILE = "genelistfile";
	public static String ARG_DENOM_FILE = "denomfile";
	public static String ARG_SPECIES = "species";
	public static String ARG_NUM_PERMUTATIONS = "numperm";
	public static String ARG_PRUNING_ALGORITHM = "pruningalgorithm";
	public static String ARG_PVAL_THRESH = "pvalthresh";
	public static String ARG_ANALYSIS_TYPE = "analysistype";
	public static String ARG_MIN_GENES_CHANGED = "mingeneschanged";
	public static String ARG_ZSCORE_THRESH = "zscorethresh";
	public static String ARG_MOD_ID_SYSTEM = "modidsystem";
	
	static String APP_SERVICE_URL = "http://webservices.cgl.ucsf.edu/opal/services/GOEliteService";
	static String OUTPUT_HEAD_URL = "http://webservices.rbvi.ucsf.edu:8080/";

	AppServicePortType service = null;
	String jobID = "";
	String jobURLString = "";
	JTable resultsTable = null; 
	Vector<String> logFileContents = new Vector<String>();
	Vector<String> stdoutFileContents = new Vector<String>();
	Vector<String> stderrFileContents = new Vector<String>();
	Vector<String> GONameResultsColumnNames = new Vector<String>();
	Vector<String> pathwayResultsColumnNames = new Vector<String>();
	//Vector<Vector> GONameResultsRowData = new Vector<Vector>();
	//Vector<Vector> pathwayResultsRowData = new Vector<Vector>();
	StatusOutputType status;
	
	public WebService() {}

	// this class talks to the opal server
	// class WebService {
	public AppServicePortType getService()
			throws javax.xml.rpc.ServiceException {
		// We could share the AppServicePortType object amongst multiple
		// requests but KISS for now
		AppServiceLocator findService = new AppServiceLocator();
		findService.setAppServicePortEndpointAddress("http://webservices.cgl.ucsf.edu/opal/services/GOEliteService");

		AppServicePortType service = findService.getAppServicePort();
		return (service);
	}
	
	public URL[] launchJob(File denomFile, File inputFile) throws IOException {
		try {
			service = getService();
			
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			
			int count = 0;
			while((line = reader.readLine()) != null) {
				count++;
			}
			
			reader.close();
			
			double v = count*0.3;
			BigDecimal bd = new BigDecimal(v);
			bd = bd.setScale(2,BigDecimal.ROUND_HALF_DOWN);
			
			jobID = launchJob(service, inputFile, denomFile, bd.intValue());
			if ( null == jobID )
			{
				return null;
			}
			String serverUrl = WebService.OUTPUT_HEAD_URL;

			jobURLString = serverUrl + jobID;

			while (status == null || 2 == status.getCode()) {
				Thread.sleep(5000);
				status = getStatus(jobID, service);
				System.out.println("[" + status.getCode() + "] " + status.getMessage() + "\n"); // shows progress in commandlines
			}
			String geneListFilePrefix = "";
			Pattern p = Pattern.compile( "(.+(\\\\|/))*(.+)\\.txt" );
			Matcher m = p.matcher( inputFile.getAbsolutePath() );
			if ( m.matches() )
			{
				geneListFilePrefix = m.group( 3 );
			}
			else
			{
				return null;
			}
			URL[] vResultURL = getResults(jobID, geneListFilePrefix, service);
			return vResultURL;
		} catch (ServiceException e3) {
			return null;
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	public static double round(double d, int decimalPlace){
	    // see the Javadoc about why we use a String in the constructor
	    // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
	    BigDecimal bd = new BigDecimal(Double.toString(d));
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_DOWN);
	    return bd.doubleValue();
	  }
	
	
	// launches a job request via the service object
		public String launchJob(AppServicePortType service, File input, File denom, int minGenes) 
		{
			try {
				if (service == null) {
					service = getService();
				}
				
				InputFileType geneListOpalFile = new InputFileType();

				// extract the name portion of the full path
				geneListOpalFile.setName(input.getName()); 
				System.out.println("processing gene list file " + geneListOpalFile.getName() + "\n");
				byte[] geneListFileBytes = Utilities.getBytesFromFile(input);

				geneListOpalFile.setContents(Utilities.replaceCR(geneListFileBytes));
				
				InputFileType denomOpalFile = new InputFileType();

				// extract the name portion of the full path
				denomOpalFile.setName(denom.getName()); 
				System.out.println("processing denom list file " + denomOpalFile.getName() + "\n");
				byte[] denomFileBytes = Utilities.getBytesFromFile(denom);
				denomOpalFile.setContents(Utilities.replaceCR(denomFileBytes));
				
				JobInputType launchJobInput = new JobInputType();

				String argList = 
					     "--species " + " Hs " +
						 "--denom " + denomOpalFile.getName() + " " + 
						 "--input " + geneListOpalFile.getName() + " " + 
						 "--mod " + "Ensembl " +
						 "--permutations " +"2000 " +
						 "--method " + "z-score " + 
						 "--pval " + "0.05 " + 
						 "--num " + minGenes + " " +
//						 "--num 3 " + 
						 "--zscore " +"1.96 ";
				
				// if ( null != statusWindow ) { statusWindow.append( argList ); }
				launchJobInput.setArgList(argList);

				// *** Wait for results, update running status
				InputFileType[] list = { geneListOpalFile, denomOpalFile };
				launchJobInput.setInputFile(list);
				JobSubOutputType output = service.launchJob(launchJobInput);

				return (output.getJobID());
			} catch (javax.xml.rpc.ServiceException e) {
				System.out.println( "Could not connect to service at " + APP_SERVICE_URL +  " : " + e.getMessage() );
				
				return (null);			
			} catch (FaultType e) {
				System.out.println( "Error during job launch/communication with webservice: " + e.getMessage() );
				return( null );
			} catch( RemoteException e ) {
				System.out.println( "Error during job launch/communication with webservice: " + e.getMessage() );
				return( null );
			} catch( java.io.IOException e ) {
				System.out.println( "Could not convert input data files to webservice-compatible format: " + e.getMessage() );
				
				return( null );
			} catch ( Exception e ) {
				System.out.println( "General exception in WebService.getResults() " + e);
				
				return( null );
			}
		}

	// service can be set to null; this will cause a new service to be located
	// and returned ( slow )
	public StatusOutputType getStatus(String jobID,
			AppServicePortType service) {
		try {
			if (service == null) {
				service = getService();
			}

			return (service.queryStatus(jobID));
		} catch (Exception e) {
			return (null);
		}
	}

	public enum ReturnTypes
	{
		RESULT_FULL_GO,
		RESULT_FULL_PATHWAY,
		RESULT_PRUNED_GO_AND_PATHWAY,
		RESULT_STDOUT,
		RESULT_STDERR,
		RESULT_LOG		
	};
	
	// service can be set to null; this will cause a new service to be located
	// and returned ( slow )
	/*
	 * returns a vector of URLs with the following elements at their respective
	 * indices: 0 - results file url 1 - log file url 2 - 
	 */

	// build the URL for each possible result, return only the ones that exist on the server
	//
	// the numeratorFilePrefix is needed to reconstruct the names of the CompleteResults/ORA/ output
	//   files on the server.  We could also grab the HTML for the directory and parse this to get those
	//   output filenames.
	public synchronized URL[] getResults(String jobID, String numeratorFilePrefix,
			AppServicePortType service) {
		try {
			numeratorFilePrefix = java.net.URLEncoder.encode( numeratorFilePrefix, "UTF-8" ).replace("+", "%20" );
			URL[] vResultURL = new URL[ ReturnTypes.values().length ];
			if (service == null) {
				service = getService();
			}
//			JobOutputType outputs = service.getOutputs(jobID);
//			OutputFileType[] files = outputs.getOutputFile();
//			URL resultsFileURL = null, logFileURL = null;
			
			String path = "";
			for ( ReturnTypes val : ReturnTypes.values() )
			{
				switch( val )
				{
					case RESULT_FULL_GO:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "GO-Elite_results/CompleteResults/ORA/" + 
							numeratorFilePrefix + "-GO.txt";
						break;
					case RESULT_FULL_PATHWAY:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "GO-Elite_results/CompleteResults/ORA/" + 
							numeratorFilePrefix + "-local.txt";
						break;
					case RESULT_PRUNED_GO_AND_PATHWAY:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "GO-Elite_results/pruned-results_z-score_elite.txt"; 
						break;
					case RESULT_STDOUT:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "stdout.txt";
						break;
					case RESULT_STDERR:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "stderr.txt";
						break;
					case RESULT_LOG:
						path = OUTPUT_HEAD_URL + "/" + jobID + "/" + "GO-Elite_report.log";
						break;
				}
 
				URL url = new URL( path );  // XXX need some sort of encoding for spaces!
//				System.out.println("url: " + url );
				if ( Utilities.exists( url ) )
					{
					 vResultURL[ val.ordinal() ] = url;
					 System.out.println(url);
					}
			}
			return (vResultURL);
		} catch (Exception e) {
			e.printStackTrace();
			return (null);
		}
	}
}

