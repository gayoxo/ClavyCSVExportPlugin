/**
 * 
 */
package fdi.ucm.server.exportparser.csv.separado.limitado;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.exportparser.csv.StaticFunctionsCSV;
import fdi.ucm.server.modelComplete.CompleteImportRuntimeException;
import fdi.ucm.server.modelComplete.ImportExportDataEnum;
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
	public static final String delimiter = ";";
	private ArrayList<ImportExportPair> Parametros;
	private String Path;
	private String FileIO;
	private String SOURCE_FOLDER = ""; // SourceFolder path
	private ArrayList<String> DateEntrada;
	

	public class Label4count{
		
		private String Label;
		private boolean count;
		
		@SuppressWarnings("unused")
		private Label4count() {
		}

		public Label4count(String label, boolean count) {
			super();
			Label = label;
			this.count = count;
		}
		
		public String getLabel() {
			return Label;
		}
		
		public boolean isCount() {
			return count;
		}
		
		
	}
	
	
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
				        tempArr = line.split(delimiter);
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

	private String ProcessCollectionCSV(CompleteCollection salvar, String sOURCE_FOLDER2,
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
				Cabecera.append(Element2LabelHash.get(buscar.get(i)).getLabel());
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
					
				for (int j = 0; j < buscar.size(); j++) 
				{
					
					
					List<String> valores = Elem2Values.get(buscar.get(j));
					
					if (valores!=null&&!valores.isEmpty())
					{
						
						if (Element2LabelHash.get(buscar.get(j)).isCount())
							Datos.append(valores.size());
						else
							if (valores.size()==1)
								Datos.append(valores.get(0));
							else
								Datos.append(Arrays.toString(valores.toArray()));

					}
					
	
					
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
	
	
	public void generateFileList(File node,List<String> fileList,
			 String SOURCE_FOLDERP) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(),SOURCE_FOLDERP));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename),fileList,SOURCE_FOLDERP);
            }
        }
    }
	
	private String generateZipEntry(String file,
			 String SOURCE_FOLDERP) {
        return file.substring(new Long((new File(SOURCE_FOLDERP)).toString().length()).intValue() + 1, file.length());
    }
	
	
	
	
	 public void zipIt(String zipFile,
			 List<String> fileList,
			 String SOURCE_FOLDERP) {
	        byte[] buffer = new byte[1024];
	        String source = new File(SOURCE_FOLDERP).getName();
	        FileOutputStream fos = null;
	        ZipOutputStream zos = null;
	        try {
	            fos = new FileOutputStream(zipFile);
	            zos = new ZipOutputStream(fos);

	            System.out.println("Output to Zip : " + zipFile);
	            FileInputStream in = null;

	            for (String file: fileList) {
	                System.out.println("File Added : " + file);
	                ZipEntry ze = new ZipEntry(source + File.separator + file);
	                zos.putNextEntry(ze);
	                try {
	                    in = new FileInputStream(SOURCE_FOLDERP + File.separator + file);
	                    int len;
	                    while ((len = in .read(buffer)) > 0) {
	                        zos.write(buffer, 0, len);
	                    }
	                } finally {
	                    in.close();
	                }
	            }

	            zos.closeEntry();
	            System.out.println("Folder successfully compressed");

	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	                zos.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	
	

	private String gramm(CompleteElementType hastype) {
		if (hastype.getCollectionFather()!=null)
			return hastype.getCollectionFather().getNombre();
		else
			if (hastype.getFather()==null)
				return hastype.getName();
			else
				return gramm(hastype.getFather());
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
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "FilteredFile"));
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public void setConfiguracion(ArrayList<String> dateEntrada) {
		DateEntrada=dateEntrada;
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
			 
			 String fileNameJSON;
				if (args.length>1)
					fileNameJSON=args[1];
				else
					fileNameJSON = null;
				
				
				
				
				
			 
			 
			 String Folder = System.getProperty("user.home")+File.separator+System.nanoTime();
		
			 new File(Folder).mkdirs();
			 
			 
//		 
//			 List<Long> List = new ArrayList<>();
//			 
//			 //36297
//			 if (args.length>1)
//					 for (int i = 1; i < args.length; i++) {
//						try {
//							List.add(Long.parseLong(args[i]));
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}


			 
			CSVSaveCollection SP=new CSVSaveCollection();
	
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
