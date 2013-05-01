// GoElitePlugin for PathVisio,
// plugin to connect to GoElite webservice
// 
// adapted from http://svn.cgl.ucsf.edu/svn/genmappcs/trunk/goeliteplugin/src/org/genmapp/goelite/Utilities.java
//
package org.wikipathways.go.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/** 
 * TODO: documentation
 */
public class Utilities {
	
	// replaces DOS-style carriage-returns with spaces: needed when sending a
	// text file from DOS -> UNIX
	public static byte[] replaceCR(byte[] bytes) {

		// first determine how many \r there are
		int j = 0;
		for ( int i = 0; i < bytes.length; i++ ) {
			if ( '\r' != bytes[ i ] )
			{
				j++;
			}
		}
		
		// now copy the data into a new buffer ( less the \r characters )
		byte[] newBytes = new byte[ j ];
		
		j = 0;  // rewind j 
		for (int i = 0; i < bytes.length; i++) {
			if ('\r' != bytes[i]) {
				newBytes[j] = bytes[i];
				j++;
			}
		}
		
		return (newBytes);
	}
	// keeps adding numeric suffixes until the name is unique
	public static String generateUniqueFilename(String filenameBase) {
		int cntr = 0;
		String x = filenameBase + ".txt";
		while (new File(x).exists()) {
			x = filenameBase + "_" + cntr + ".txt";
			cntr++;
		}
		return (x);
	}

	public static boolean exists( URL u ){
	    try {
	      String URLName = u.toString();
	      HttpURLConnection.setFollowRedirects(false);
	      // note : you may also need
	      //        HttpURLConnection.setInstanceFollowRedirects(false)
	      HttpURLConnection con =
	         (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");

	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       return false;
	    }
	}
	public static Vector<String> getFileContents(URL u)
	throws MalformedURLException, IOException {
		Vector<String> contents = new Vector<String>();
		
		InputStream is = null;
		BufferedReader bf = null;
		
		// ----------------------------------------------//
		// Step 3: Open an input stream from the
		// url. //
		// ----------------------------------------------//
		
		is = u.openStream(); // throws an
		// IOException
		//-------------------------------------------------------------/
		// /
		
		// Step 4: //
		//-------------------------------------------------------------/
		// /
		// Convert the InputStream to a buffered
		// DataInputStream. //
		// Buffering the stream makes the reading
		// faster; the //
		// readLine() method of the DataInputStream
		// makes the reading //
		// easier. //
		//-------------------------------------------------------------/
		// /
		
		bf = new BufferedReader(new InputStreamReader(is));
		
		//------------------------------------------------------------//
		// Step 5: //
		//------------------------------------------------------------//
		// Now just read each record of the input
		// stream, and print //
		// it out. Note that it's assumed that this
		// problem is run //
		// from a command-line, not from an
		// application or applet. //
		//------------------------------------------------------------//
		
		String s = null;
		while ((s = bf.readLine()) != null) {
			contents.add(s);
		}
		
		return (contents);
	}
	
	
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException( "File too large to send in buffer" );
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	public static SortedMap<String,String> getOrganisms() 
	{
		String bridgedbSpecieslist = "http://svn.bigcat.unimaas.nl/bridgedb/trunk/org.bridgedb.bio/resources/org/bridgedb/bio/organisms.txt";
		Vector<String> lines = null;
		
		try 
		{
			lines = Utilities.getFileContents(new URL(bridgedbSpecieslist));
		} catch (java.net.MalformedURLException e) 
		{
			System.out.println("Malformed URL in species list\t" + e.getMessage());
			return null;
		
		} catch (java.io.IOException e) 
		{ 
			System.out.println("Could not retrieve species list from URL\t" + e.getMessage());
			return null;
		}
		
		SortedMap<String,String> goeSpecies = new TreeMap<String,String>();
		
		for(String str : lines)
		{
			String [] buffer = str.split("\t");
			int length = buffer.length;
			String name = buffer[0];
			if(length > 3) name = name + " " + buffer[1];
			if(length > 4) name = name + " " + buffer[2];
			goeSpecies.put(name, buffer[length-1]);
		}
		
		return goeSpecies;
	}
} 