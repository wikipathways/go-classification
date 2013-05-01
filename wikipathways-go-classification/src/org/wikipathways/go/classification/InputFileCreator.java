package org.wikipathways.go.classification;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.XrefIterator;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.wikipathways.client.WikiPathwaysClient;

public class InputFileCreator {
	
	public static int createInputFile(IDMapper mapper, File file, String pathwayId, WikiPathwaysClient client) {
		try {
			WSPathway wsPathway = client.getPathway(pathwayId);
			Pathway pathway = new Pathway();
			InputStream is = new ByteArrayInputStream(wsPathway.getGpml().getBytes());
			pathway.readFromXml(is, true);
			Set<String> genes = new HashSet<String>();
			for(Xref xref : pathway.getDataNodeXrefs()) {
				try {
					if(xref.getId() != null && xref.getDataSource() != null) {
						Set<Xref> result = mapper.mapID(xref, DataSource.getBySystemCode("En"));
						for(Xref x : result) {
							genes.add(x.getId());
						}
					}
				} catch (IDMapperException e) {
				}
			}
			if(genes.size() > 10) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				for(String str : genes) {
					writer.write(str + "\tEn\n");
				}
				writer.close();
				return genes.size();
			} else {
				return genes.size();
			}
		} catch (ConverterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void createDenominatorFile(IDMapper mapper, File file) throws IDMapperException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		if(mapper instanceof XrefIterator) { 
			DataSource ds = DataSource.getBySystemCode("En");
			for (Xref x : ((XrefIterator) mapper).getIterator()) { 
			  if(x.getDataSource().equals(ds)) {
				  writer.write(x.getId() + "\t" + x.getDataSource().getSystemCode() +"\n");
			  }
			} 
		}
		
		writer.close();
	}

}
