/**
 * 
 */
package fdi.ucm.server.exportparser.csv.separado.limitado;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;

/**
 * Clase que impementa el plugin de oda para Localhost
 * @author Joaquin Gayoso-Cabada
 *
 */
public class CSVSaveCollectionMnemosine extends CSVSaveCollection {

	
	

	
 
	
	public static void main(String[] args) {
		
		String message="Exception .clavy-> Params Null ";
		try {

			String fileName;
			if (args.length>0)
				fileName=args[0];
			else
				fileName = "test.clavy";
			 
			System.out.println(fileName);
			 

			 File file = new File(fileName);
			 FileInputStream fis = new FileInputStream(file);
			 ObjectInputStream ois = new ObjectInputStream(fis);
			 CompleteCollection object = (CompleteCollection) ois.readObject();
			 
			 
			 try {
				 ois.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			 try {
				 fis.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			 
			 String fileNameJSON="mnemosine.csv";

			 
			 String Folder = System.getProperty("user.home")+File.separator+System.nanoTime();
		
			 new File(Folder).mkdirs();
			 
			 
			 
			CSVSaveCollectionMnemosine SP=new CSVSaveCollectionMnemosine();
	
			ArrayList<String> lisyta=new ArrayList<String>();
			lisyta.add(fileNameJSON);
			
			SP.setConfiguracion(lisyta);
			SP.processCollecccion(object,Folder);
			 
	    }catch (Exception e) {
			e.printStackTrace();
			System.err.println(message);
			throw new RuntimeException(message);
		}
		  
		  
	}

	
	
	
	
}
