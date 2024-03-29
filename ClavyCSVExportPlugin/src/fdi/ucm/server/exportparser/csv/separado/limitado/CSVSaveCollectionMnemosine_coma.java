/**
 * 
 */
package fdi.ucm.server.exportparser.csv.separado.limitado;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
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
public class CSVSaveCollectionMnemosine_coma extends CSVSaveCollection {

	
	public static final String delimiterin = ";";
	public static final String delimiter = ",";
	public static final String quote = "\"";

	
	
	/* (non-Javadoc)
	 * @see fdi.ucm.server.SaveCollection#processCollecccion(fdi.ucm.shared.model.collection.Collection)
	 */
	@Override
	public CompleteLogAndUpdates processCollecccion(CompleteCollection Salvar,
			String PathTemporalFiles) throws CompleteImportRuntimeException{
		try {
			
			HashMap<String, HashMap<String, Label4count>> Grama2Elem2Visual=new HashMap<String, HashMap<String,Label4count>>();
			
			if (DateEntrada.size()>0)
			{
				String fileNameCSV = DateEntrada.get(0);
				
				try {
				      File file = new File(fileNameCSV);
				      FileReader fr = new FileReader(file);
				      BufferedReader br = new BufferedReader(fr);
				      String line = " ";
				      String[] tempArr;
				      while ((line = br.readLine()) != null) {
				        tempArr = line.split(delimiterin);
				        if (tempArr.length>1)
				        {
				        	String Grammar = tempArr[0].trim().toLowerCase();
				        	String Elem = tempArr[1].trim().toLowerCase();
				        	String Label = Elem;
				        	boolean count = false;
				        	if (tempArr.length>2)
				        		Label=tempArr[2].trim().toLowerCase();
				        	if (tempArr.length>3)
				        		try {
				        			count=Boolean.parseBoolean(tempArr[3].trim().toLowerCase());
								} catch (Exception e) {
									// TODO: handle exception
								}

				        	
				        	HashMap<String, Label4count> ListaActualGramar = Grama2Elem2Visual.get(Grammar);
				        	if (ListaActualGramar==null)
				        		ListaActualGramar=new HashMap<String, Label4count>();
				        	
				        	ListaActualGramar.put(Elem, new Label4count(Label, count));
				        	Grama2Elem2Visual.put(Grammar, ListaActualGramar);
				        	
				        }
					       
				        
				      
				      }
				      br.close();
				    }
				    catch(IOException ioe) {
				      ioe.printStackTrace();
				    }
					catch(NullPointerException ioe) {
				      ioe.printStackTrace();
				    }
			}

			
			CompleteLogAndUpdates CL=new CompleteLogAndUpdates();

			
			Path=PathTemporalFiles;
			SOURCE_FOLDER=Path+File.separator;
			File Dir=new File(SOURCE_FOLDER);
			Dir.mkdirs();
			
			
			FileIO =ProcessCollectionCSV(Salvar,SOURCE_FOLDER,CL,Grama2Elem2Visual);
	

			CL.getLogLines().add("Descarga el csv");
			return CL;


		} catch (CompleteImportRuntimeException e) {
			System.err.println("Exception HTML " +e.getGENERIC_ERROR());
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	@Override
	protected String ProcessCollectionCSV(CompleteCollection salvar, String sOURCE_FOLDER2,
			CompleteLogAndUpdates cL, HashMap<String, HashMap<String, Label4count>> grama2Elem2Visual) {
		
		
		
		String sOURCE_FOLDER_CSV = sOURCE_FOLDER2+File.separator+"CSV"+File.separator;
		File Dir=new File(sOURCE_FOLDER_CSV);
		Dir.mkdirs();
		
		
		if (grama2Elem2Visual.isEmpty())
			for(CompleteGrammar CG:salvar.getMetamodelGrammar())
				{
				HashMap<String, Label4count> TablaLabel = new HashMap<String, Label4count>();
				
				List<CompleteElementType> Salida=new LinkedList<>();
				Salida.addAll(generalistaS(CG.getSons()));
				
				for (CompleteElementType completeElementType : Salida) {
					if (completeElementType instanceof CompleteTextElementType)
						TablaLabel.put(completeElementType.getName().trim().toLowerCase(),
								new Label4count(completeElementType.getName().trim().toLowerCase(), false));
					else
						TablaLabel.put(completeElementType.getName().trim().toLowerCase(),
								new Label4count(completeElementType.getName().trim().toLowerCase(), true));
				}
				
				
				
				grama2Elem2Visual.put(CG.getNombre().trim().toLowerCase(), TablaLabel);
				
				
				
				
				}
			
		
		
		HashMap<String, StringBuilder> Lista2File=new HashMap<String, StringBuilder>();
		
		for (String gramarfilename : grama2Elem2Visual.keySet())
			Lista2File.put(gramarfilename, new StringBuilder());


		for (Entry<String, HashMap<String, Label4count>> gramarfilename : grama2Elem2Visual.entrySet())
		{
			String Grammarname = gramarfilename.getKey();
			HashMap<String, Label4count> Element2LabelHash = gramarfilename.getValue();
			
			
			StringBuilder Cabecera=new StringBuilder();
			StringBuilder Datos=new StringBuilder();

			List<String> buscar=new LinkedList<String>(Element2LabelHash.keySet());
			
			Cabecera.append("clavy_id");
			Cabecera.append(delimiter);
			
			for (int i = 0; i < buscar.size(); i++) 
				{
				String ValueCAb=Element2LabelHash.get(buscar.get(i)).getLabel();
				Cabecera.append(double_quote(ValueCAb));
				if ((i+1) < buscar.size())
					Cabecera.append(delimiter);
				}
			
			for (int i = 0; i < salvar.getEstructuras().size(); i++) {
				CompleteDocuments completeElementType = salvar.getEstructuras().get(i);
				HashMap<String, List<String>> Elem2Values=new HashMap<String, List<String>>();
				for (CompleteElement Elemento : completeElementType.getDescription()) {
					if (gramm(Elemento.getHastype()).toLowerCase().equals(Grammarname))
						if (Element2LabelHash.get(Elemento.getHastype().getName().trim().toLowerCase())!=null)
					{
							List<String> Values=Elem2Values.get(Elemento.getHastype().getName().trim().toLowerCase());
							if (Values==null)
								Values=new LinkedList<String>();
							if (Elemento instanceof CompleteTextElement)
								Values.add(((CompleteTextElement) Elemento).getValue());
							else
								Values.add(Integer.toString(i));
							Elem2Values.put(Elemento.getHastype().getName().trim().toLowerCase(), Values);
					}
				}
				
				
				
				
				
				if (!Elem2Values.isEmpty()) {
					
					Datos.append(completeElementType.getClavilenoid());	
					Datos.append(delimiter);
					
					
					if (Element2LabelHash.get("des")!=null)
					{
						List<String> Values=Elem2Values.get("des");
						if (Values==null)
							Values=new LinkedList<String>();

						
							Values.add(completeElementType.getDescriptionText());

						Elem2Values.put("des", Values);
					}
					
					
					
					
					
				for (int j = 0; j < buscar.size(); j++) 
				{
					
					
					List<String> valores = Elem2Values.get(buscar.get(j));
					
					if (valores!=null&&!valores.isEmpty())
					{
						
						String ValorMete="";
						if (Element2LabelHash.get(buscar.get(j)).isCount())
							ValorMete=Integer.toString(valores.size());
						else
							if (valores.size()==1)
								ValorMete=valores.get(0);
							else
								ValorMete=Arrays.toString(valores.toArray());

						ValorMete=ValorMete.replace("\n", "").replace("\r", "").trim();
						
						Datos.append(double_quote(ValorMete));
					}
					else
						if (Element2LabelHash.get(buscar.get(j)).isCount())
							Datos.append(0);
					
	
					
					
				if ((j+1) < buscar.size())
					Datos.append(delimiter);
				}
			
				Datos.append('\n');
				}
			}
			
			
			if (Datos.length()>0)
			{
				StringBuilder MIoFile=Lista2File.get(Grammarname);
				MIoFile.append(Cabecera.toString());
				MIoFile.append('\n');
				MIoFile.append(Datos.toString());
			}
		}
		
		
		
		
		for (String gramarfilename : grama2Elem2Visual.keySet()) {
			StringBuilder StringB = Lista2File.get(gramarfilename);
			if (StringB.length()>0)
				{
				
				try {
					String S=sOURCE_FOLDER_CSV+File.separatorChar+gramarfilename+".csv";
					 PrintWriter pw=null;
					 pw = new PrintWriter(new File(S));
					 
					 pw.write(StringB.toString());
				     pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				
				}
		}
		
		List<String> fileList=new LinkedList<String>();
		generateFileList(new File(sOURCE_FOLDER_CSV), fileList, sOURCE_FOLDER_CSV);
		
		String sOURCE_FOLDERSalida = sOURCE_FOLDER2+File.separatorChar+System.currentTimeMillis()+"_CSV_"+salvar.getClavilenoid()+".zip";
		
		zipIt(sOURCE_FOLDERSalida, fileList, sOURCE_FOLDER_CSV);
		
	        System.out.println("done!");
	
		
		
		return sOURCE_FOLDERSalida;
	}
 
	
	private String double_quote(String valueCAb) {
		valueCAb=valueCAb.replace("\"", "\"\"");
		return quote+valueCAb+quote;
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
			 
			 String fileNameJSON="mnemosine.csv";

			 
			 String Folder = System.getProperty("user.home")+File.separator+System.nanoTime();
		
			 new File(Folder).mkdirs();
			 
			 
			 
			CSVSaveCollectionMnemosine_coma SP=new CSVSaveCollectionMnemosine_coma();
	
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
