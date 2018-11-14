/**
 * 
 */
package fdi.ucm.server.exportparser.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
import fdi.ucm.server.modelComplete.SaveCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * Clase que impementa el plugin de oda para Localhost
 * @author Joaquin Gayoso-Cabada
 *
 */
public class CSVSaveCollection extends SaveCollection {

	private static final String CSV = "CSV file";
	private ArrayList<ImportExportPair> Parametros;
	private String Path;
	private String FileIO;
	private String SOURCE_FOLDER = ""; // SourceFolder path

	
	/**
	 * Constructor por defecto
	 */
		public CSVSaveCollection() {
	}

	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#processCollecccion(fdi.ucm.shared.model.collection.Collection)
	 */
	@Override
	public CompleteLogAndUpdates processCollecccion(CompleteCollection Salvar,
			String PathTemporalFiles) throws CompleteImportRuntimeException{
		try {
			
			CompleteLogAndUpdates CL=new CompleteLogAndUpdates();
			
//			if (!ListaDeDocumentos.isEmpty())
//			{
			
			Path=PathTemporalFiles;
			SOURCE_FOLDER=Path+File.separator;
			File Dir=new File(SOURCE_FOLDER);
			Dir.mkdirs();
			
			
			FileIO =ProcessCollectionCSV(Salvar,SOURCE_FOLDER,CL);
	

			CL.getLogLines().add("Descarga el csv");
			return CL;
//			}
//			else 
//			{
//				CL.getLogLines().add("Error in list, numeber of documents empty");
//				return CL;
//			}

		} catch (CompleteImportRuntimeException e) {
			System.err.println("Exception HTML " +e.getGENERIC_ERROR());
			e.printStackTrace();
			throw e;
		}
		
	}

	private String ProcessCollectionCSV(CompleteCollection salvar, String sOURCE_FOLDER2, CompleteLogAndUpdates cL) {
		List<CompleteGrammar> GramarValid= new LinkedList<CompleteGrammar>();
		for (CompleteGrammar completeGrammar : salvar.getMetamodelGrammar()) {
			if (!StaticFunctionsCSV.IsIgnore(completeGrammar.getViews()))
				GramarValid.add(completeGrammar);
		}
		
		List<CompleteElementType> lista=new ArrayList<>();
		lista=generalista(GramarValid);
		String S=sOURCE_FOLDER2+File.separatorChar+System.nanoTime()+".csv";
		 PrintWriter pw=null;
		try {
			pw = new PrintWriter(new File(S));
		
	        StringBuilder sb = new StringBuilder();
	        
	        boolean primero=true;
	        
	        for (CompleteElementType completeElementType : lista) {
	        	if (primero)
	        		primero=false;
	        	else
	        		sb.append(',');	
       
		        sb.append("\""+completeElementType.getName()+"("+completeElementType.getClavilenoid()+")"+"\"");
		        
			}
	        
	        sb.append('\n');
	        
	        for (CompleteDocuments completeElementType : salvar.getEstructuras()) {
	        	boolean found=false;
	        	HashMap<CompleteElementType, String> tablaval=new HashMap<>();
				for (CompleteElement completeElementTypeD : completeElementType.getDescription()) {
					if (completeElementTypeD.getHastype()!=null
							&&lista.contains(completeElementTypeD.getHastype())
							&&completeElementTypeD instanceof CompleteTextElement
							&&!((CompleteTextElement)completeElementTypeD).getValue().trim().isEmpty())
						{
						found=true;
						tablaval.put(completeElementTypeD.getHastype(), ((CompleteTextElement)completeElementTypeD).getValue());
						}
					
				}
				
				if (found)
				{
					 boolean primeroe=true;
				for (CompleteElementType completeElementType2 : lista) {
					if (primeroe)
		        		primeroe=false;
		        	else
		        		sb.append(',');	
					
					String valueS=tablaval.get(completeElementType2);
					if (valueS!=null&&!valueS.trim().isEmpty())
						{
						valueS=valueS.replace(',', '_');
						valueS=valueS.replace('\n', '_');
						valueS=valueS.replace('\t', '_');
						valueS=valueS.replace('\r', '_');
						sb.append(valueS);
						}
					else
						sb.append("");
				}	
					
				 sb.append('\n');
				}
				
				
			}
	        
//	        sb.append("id");
//	        sb.append(',');
//	        sb.append("Name");
//	        sb.append('\n');
//
//	        sb.append("1");
//	        sb.append(',');
//	        sb.append("Prashant Ghimire");
//	        sb.append('\n');

	        pw.write(sb.toString());
	        pw.close();
	        System.out.println("done!");
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		
		
		return S;
	}

	private List<CompleteElementType> generalista(List<CompleteGrammar> gramarValid) {
		List<CompleteElementType> Salida=new LinkedList<>();
		for (CompleteGrammar completeElementType : gramarValid) {
			
			Salida.addAll(generalistaS(completeElementType.getSons()));
		}
		return Salida;
	}

	private List<CompleteElementType> generalistaS(List<CompleteElementType> sons) {
		List<CompleteElementType> Salida=new LinkedList<>();
		for (CompleteElementType completeElementType : sons) {
			if (completeElementType instanceof CompleteTextElementType
					&&!StaticFunctionsCSV.IsIgnore(completeElementType.getShows()))
				Salida.add(completeElementType);
			if (!StaticFunctionsCSV.IsIgnore_Sons(completeElementType.getShows()))
				Salida.addAll(generalistaS(completeElementType.getSons()));
		}
		
		return Salida;
	}

	/**
	 * QUitar caracteres especiales.
	 * @param str texto de entrada.
	 * @return texto sin caracteres especiales.
	 */
	public String RemoveSpecialCharacters(String str) {
		   StringBuilder sb = new StringBuilder();
		   for (int i = 0; i < str.length(); i++) {
			   char c = str.charAt(i);
			   if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
			         sb.append(c);
			      }
		}
		   return sb.toString();
		}

	


	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public void setConfiguracion(ArrayList<String> DateEntrada) {

	}
		


	@Override
	public String getName() {
		return CSV;
	}


	@Override
	public boolean isFileOutput() {
		return true;
	}

	@Override
	public String FileOutput() {
		return FileIO;
	}

	@Override
	public void SetlocalTemporalFolder(String TemporalPath) {
		
	}

	

	

	
 
	
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
			 
			 String Folder = System.getProperty("user.home")+File.separator+System.nanoTime();
		
			 new File(Folder).mkdirs();
			 
			 
		 
			 List<Long> List = new ArrayList<>();
			 
			 //36297
			 if (args.length>1)
					 for (int i = 1; i < args.length; i++) {
						try {
							List.add(Long.parseLong(args[i]));
						} catch (Exception e) {
							// TODO: handle exception
						}
					}


			 
			CSVSaveCollection SP=new CSVSaveCollection();
			SP.processCollecccion(object,Folder);
			 
	    }catch (Exception e) {
			e.printStackTrace();
			System.err.println(message);
			throw new RuntimeException(message);
		}
		  
		  
	}

	
	
	
	
}
