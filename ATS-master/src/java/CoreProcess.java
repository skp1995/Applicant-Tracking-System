/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import javax.servlet.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hwpf.usermodel.HeaderStories;
import org.apache.poi.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.nio.file.*;
/**
 *
 * @author Vishnu
 * @author Krishnan Arun
 * @author Shreekrishna Prasad
 */
public class CoreProcess {
        //ArrayList to load all the stop words
    public static ArrayList<String> stopList=new ArrayList<String>();    
    public static Hashtable<String, Double> techScoringWords = new Hashtable<String, Double>();
    public static Hashtable<String, Double> nontechScoringWords = new Hashtable<String, Double>();
    public static Hashtable<String, Double> expScoringWords = new Hashtable<String, Double>();
    public static Hashtable<String, Double> techScores=new Hashtable<String,Double>();
    public static Hashtable<String, Double> nontechScores=new Hashtable<String,Double>();
    public static Hashtable<String, Double> expScores=new Hashtable<String,Double>();  
    public static Hashtable<String, Double> finalScores=new Hashtable<String, Double>();
    public static String overallResumePath;
    //AHP Variables
    private static final int SIZE=3;
    public static double[][] arr=new double [SIZE][SIZE];
    public static double[][] ahpResult=new double[SIZE][1];
    
    /**
     * 
     * @param arr input relative weights matrix from input form.
     * @param w data structure to store the weights matrix.
     * @return true if the array is consistent, false otherwise.
     */
    
    public static boolean AHP(double[][] arr, double[][] w)
    {
        return process(arr,w);
    }

    /**
     * 
     * @param arr The input matrix containing relative weights.
     * @param w The weights containing the resultant absolute weights.
     * @return true if the array is consistent, false otherwise.
     */
    
    private static boolean process(double arr[][], double w[][])
    {
            int i,j;
            double[][] copy=new double[SIZE][SIZE];

            for(j=0;j<SIZE;j++)
            {
                    for(i=0;i<SIZE;i++)
                    {
                            copy[j][i]=arr[j][i];
                    }
            }
            
            double sum=0;
            for(j=0;j<SIZE;j++)
            {
                    sum=0;
                    for(i=0;i<SIZE;i++)
                    {
                            sum+=arr[i][j];
                    }
                    for(i=0;i<SIZE;i++)
                    {
                            arr[i][j]/=sum;
                    }
            }		

            for(j=0;j<SIZE;j++)
            {
                    for(i=0;i<SIZE;i++)
                    {
                            w[j][0]+=arr[j][i];
                    }
                    w[j][0]/=SIZE;
            }

            if(consistencyCheck(copy,w)==true)
            {
                for(j=0;j<SIZE;j++)
                {
                    ahpResult[j][0]=w[j][0];
                }
                return true;
            }
            return false;
            
    }

    /**
     * 
     * @param arr The input matrix containing relative weights.
     * @param w The weights containing the resultant absolute weights.
     * @return true if the array is consistent, false otherwise.
     */
    
    private static boolean consistencyCheck(double arr[][], double w[][])
    {
            double[][] Ws=new double[SIZE][1];
            matrixMul(arr,w,Ws,SIZE,SIZE,1);
//            System.out.println("\nWs:");
//            display2D(Ws,SIZE,1);
            double[][] consis=new double[SIZE][1];
            double lambda=0,CI=0,RI,CR;
            for(int i=0;i<SIZE;i++)
                    {
                            consis[i][0]= Ws[i][0]*(1/w[i][0]);
                            lambda+=consis[i][0];
                    }
            lambda/=SIZE;

            CI=((lambda-SIZE)/(SIZE-1));

            RI=1.25;
            CR=CI/RI;

           
            if(CR< 0.10)
            {
                    return true;
            }
            else
            {
                    return false;
            }
    }

    /**
     * A function for matrix multiplication.
     * @param arr The first matrix of order m*n
     * @param w The second matrix of order n*u
     * @param res The resultant matrix of order m*u
     * @param m Number of rows of arr
     * @param n Number of columns of arr=Number of rows of w
     * @param u Number of columns of w
     */
    
    private static void matrixMul(double arr[][], double[][] w, double[][] res,int m,int n, int u)
    {
            int k=0;
            for(int i=0;i<m;i++)
            {
                    for(int j=0;j<u;j++)
                    {
                            res[i][j]=0;
                            for(k=0;k<n;k++)
                                    res[i][j]+=(arr[i][k]*w[k][j]);
                    }
            }

    }
    
    /**
     * A function to read stop words and store in a local hash-table.
     * @param path Path to the '.txt' file containing the stop words.
     * @throws Exception 
     */

    public static void loadStopWords(String path) throws Exception
    {
        FileReader fr=new FileReader(path);
        BufferedReader br=new BufferedReader(fr);

        String readText="";

        while((readText=br.readLine())!=null)
        {
                stopList.add(readText);
        }	    

        //System.out.println("Stop Words Loaded\n");

        fr.close();

    }
    
    /**
     * A function to read a particular source file specified by path and name, convert to .txt, remove stop words and write into destPath.
     * @param filePath Path to the folder containing the source files
     * @param fileName Name of the source file being read inside filePath.
     * @param destPath The destination folder path to write the file into.
     * @throws Exception 
     */
    
    private static void readSourceFile(String filePath, String fileName, String destPath) throws Exception
    {
        File file= new File(filePath+'/'+fileName);
        String text="";
        PDDocument document=PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();

                //Retrieving text from PDF document
            text += pdfStripper.getText(document);
            

                //filtering
        text = text		    	
                .replaceAll("\\&"," ")
                .replaceAll("\\!"," ")
                .replaceAll("\\+"," ")
                .replaceAll("\\."," ")
                .replaceAll("[^A-Za-z0-9. ]+", " ")
                .replaceAll("\\p{P}", " ")
                .replaceAll("\\d"," ")
                .replaceAll("( )+", " ")
                .replaceAll("[\\t\\n\\r]"," ")
                .replaceAll("(\\t|\\r?\\n)+", " ")
                .toLowerCase();

        String newText="";
        String token[]=text.split(" ");
        int length=token.length;
        String path="";
        for(int i=0;i<length;i++)
        {
                if(stopList.contains(token[i]))
                {

                }
                else
                {
                        newText+=token[i]+" ";
                }	
        }

        fileName=fileName.replace(".pdf","");
        FileOutputStream textFile=new FileOutputStream(destPath+fileName+".txt");
       /* FileWriter fw=new FileWriter(path,append);
        PrintWriter pw=new PrintWriter(fw);
        pw.printf("%s"+"%n", newText);
        pw.close();
               
        */
        
        textFile.write(newText.getBytes());
        textFile.close();
        document.close();

        //Closing the document
      //  fw.close();        
    }
    
    /**
     * A function to convert a resume to .txt, remove stop words and store the file in destPath.
     * @param resumePath Path to folder containing resumes
     * @param destPath Destination path to store processed resumes
     * @throws Exception 
     */
    
    public static void loadFiles(String resumePath, String destPath) throws Exception
    {
        File folder = new File(resumePath);
        File[] listOfFiles = folder.listFiles();

        FileWriter fw=new FileWriter(destPath, false);
        PrintWriter pw=new PrintWriter(fw); 


        //List of files names in the directory
        System.out.println("Loading resumes.");
        for (int i = 0; i < listOfFiles.length; i++) 
        {
                if(listOfFiles[i].isDirectory())
                {
                    loadFiles(listOfFiles[i].getAbsolutePath(),destPath);
                }            
                if (listOfFiles[i].isFile()) 
                {
                        
                    pw.println( listOfFiles[i].getName());
                } 

        }
        pw.close();
        fw.close();
    }
    
    /**
     * A function to process resumes specified by resumePath and to store in destPath.
     * @param resumePath Path to folder containing resumes
     * @param destPath Destination Path
     * @throws Exception 
     */
    
    public static void processResumes(String resumePath, String destPath) throws Exception
    {
        overallResumePath=resumePath;
        File folder=new File(resumePath);
        String readText="";
        int docs=folder.listFiles().length;
        for (final File fileEntry : folder.listFiles()) 
        {
            if (fileEntry.isDirectory()) 
            {
                processResumes(fileEntry.getAbsolutePath(),destPath);
            }
            else
            {
                readResumeFile(resumePath,fileEntry.getName(),destPath);
            }
        }
    }
    
    /**
     * A function to access source files, convert to .txt, remove stop words, calculate TF-IDF score and write to destination Path.
     * @param folder Folder containing source files
     * @param destPath Destination folder path
     * @param flag Denotes if technical, non-technical or experience related source files are read.
     * @throws Exception 
     */
    
    public static void accessSourceFiles(String folder, String destPath, int flag ) throws Exception
    {
        HashMap<String, Integer> TFscore=new HashMap<String, Integer>();
        HashMap<String, Integer> wordDoc=new HashMap<String,Integer>();
        HashMap<String, Double> tfidfscore=new HashMap<String, Double>();	
        File techfolder=new File(folder);    
            //All words encountered so far in a document
            ArrayList<String> wordOccur=new ArrayList<String>();
            int docs=techfolder.listFiles().length;
            for (final File fileEntry : techfolder.listFiles()) 
            {
                    if (fileEntry.isDirectory()) 
                    {
                        accessSourceFiles(fileEntry.getAbsolutePath() ,destPath, flag);
                    }
                    else
                    {
                        //System.out.println(fileEntry.getName());    
                        readSourceFile(folder, fileEntry.getName(), destPath);
                        String fn=fileEntry.getName().replace(".pdf",".txt");
                        BufferedReader br=new BufferedReader(new FileReader(destPath+fn));
                        wordOccur.clear();
                        wordOccur=new ArrayList<String>();
                        String line;
                        String[] tokens;
                        while((line=br.readLine())!=null)
                        {
                                tokens=line.split(" ");
                                for(int i=0;i<tokens.length;++i)
                                {
                                        if(!(wordOccur.contains(tokens[i])))
                                        {
                                                wordOccur.add(tokens[i]);
                                                if(wordDoc.containsKey(tokens[i]))
                                                {
                                                        wordDoc.put(tokens[i],wordDoc.get(tokens[i])+1); //occurance in other docs
                                                }
                                                else
                                                {
                                                        wordDoc.put(tokens[i],1);	//first time occurance
                                                }
                                        }

                                        if(TFscore.containsKey(tokens[i]))
                                        {
                                                TFscore.put(tokens[i],TFscore.get(tokens[i])+1);
                                        }
                                        else
                                        {
                                                TFscore.put(tokens[i],1);
                                        }
                                        //System.out.println(tokens[i]);
                                }
                        
                        }
                    }
            }
            Iterator it = TFscore.entrySet().iterator();
            while (it.hasNext()) 
            {
                Map.Entry<String,Object> pair = (Map.Entry)it.next();
                String key=pair.getKey();
                double idf=Math.log(docs/wordDoc.get(key)); //Do log to base 2 later on.
                tfidfscore.put(key,(TFscore.get(key)*idf));
            }	
            it=tfidfscore.entrySet().iterator();
            double val1,val2,val3;
            while(it.hasNext())
            {
                    Map.Entry<String,Double> pair=(Map.Entry)it.next();
                    String key=pair.getKey();
                    if(flag==1)
                        techScoringWords.put(key, pair.getValue());
                    if(flag==2)
                        nontechScoringWords.put(key, pair.getValue());
                    if(flag==3)
                        expScoringWords.put(key, pair.getValue());
                    if(techScoringWords.containsKey(key) && nontechScoringWords.containsKey(key) && expScoringWords.containsKey(key))
                    {
                        val1=techScoringWords.get(key);
                        val2=nontechScoringWords.get(key);
                        val3=expScoringWords.get(key);
                        if(val1>val2 && val1>val3)
                        {
                            nontechScoringWords.remove(key);
                            expScoringWords.remove(key);
                        }
                        else if(val2>val3 && val2>val1)
                        {
                            techScoringWords.remove(key);
                            expScoringWords.remove(key);                    
                        }
                        else if(val3>val1 && val3>val2)
                        {
                            nontechScoringWords.remove(key);
                            techScoringWords.remove(key);                    
                        }
                    }
            }
    }
    
    /**
     * A function that writes tech words into destination file from the local hash table.
     * @param destPath Destination Path to write tech words into.
     * @throws Exception 
     */
    
    public static void loadTechWords(String destPath) throws Exception
    {
        FileWriter fw = new FileWriter(destPath,false);
        BufferedWriter bw = new BufferedWriter(fw);
        Iterator it = techScoringWords.entrySet().iterator();
        String key;
        while (it.hasNext()) 
        {
            Map.Entry<String,Double> pair=(Map.Entry)it.next();
            key=pair.getKey();
            bw.write(key+" "+pair.getValue()+" ");
            bw.newLine();            
        }
    }
    
    /**
     * A function that writes non-tech words into destination file from the local hash table.
     * @param destPath Destination Path to write non-tech words into.
     * @throws Exception 
     */
    
    public static void loadNontechWords(String destPath) throws Exception
    {
        FileWriter fw = new FileWriter(destPath,false);
        BufferedWriter bw = new BufferedWriter(fw);
        Iterator it = nontechScoringWords.entrySet().iterator();
        String key;
        while (it.hasNext()) 
        {
            Map.Entry<String,Double> pair=(Map.Entry)it.next();
            key=pair.getKey();
            bw.write(key+" "+pair.getValue()+" ");
            bw.newLine();            
        }
    }
    
    /**
     * A function that writes experience words into destination file from the local hash table.
     * @param destPath Destination Path to write experience related words into.
     * @throws Exception 
     */
    
    public static void loadExpWords(String destPath) throws Exception
    {
        FileWriter fw = new FileWriter(destPath,false);
        BufferedWriter bw = new BufferedWriter(fw);
        Iterator it = expScoringWords.entrySet().iterator();
        String key;
        while (it.hasNext()) 
        {
            Map.Entry<String,Double> pair=(Map.Entry)it.next();
            key=pair.getKey();
            bw.write(key+" "+pair.getValue()+" ");
            bw.newLine();            
        }
        
    }
    
    /**
     * A function to read a particular resume file, convert to .txt, remove stop words and write to destination path.
     * @param filePath Path to the resume file
     * @param fileName Name of the file
     * @param destPath Destination path
     * @throws Exception 
     */
    
    public static void readResumeFile(String filePath, String fileName, String destPath) throws Exception 
    {
        // code application logic here

        File file = new File(filePath+'/'+fileName);
        //PDDocument document=null;
        //PDFTextStripper pdfStripper;
        //XWPFDocument doc1=null;
        //XWPFWordExtractor extractor=null;
        String text="",text1="",text2="",text3="";
        int flag=0;
        
        String extension = "";
        int pos=fileName.lastIndexOf('.');
        if(pos>0)
            extension=fileName.substring(pos+1);
        if(extension.equals("pdf"))
        {
            PDDocument document=PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();

        //Retrieving text from PDF document
            text = pdfStripper.getText(document);
            document.close();
        }
        else if(extension.equals("docx"))
        {
            FileInputStream fis=new FileInputStream(file.getAbsolutePath());            
            XWPFDocument doc1=new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc1);            
            text = extractor.getText();                        
            doc1.close();
            flag=1;
        }
        else if(extension.equals("doc"))
        {
            FileInputStream fis=new FileInputStream(file.getAbsolutePath());
            HWPFDocument doc=new HWPFDocument(fis);
            WordExtractor ext=new WordExtractor(doc);
            text = ext.getText();
            doc.close();            
            flag=2;
        }
//        //filtering the text; deleting all special symbols
        text = text		    	
                .replaceAll("\\&"," ")
                .replaceAll("\\!"," ")
                .replaceAll("\\+"," ")
                .replaceAll("\\."," ")
                .replaceAll("[^A-Za-z0-9. ]+", " ")
                .replaceAll("\\p{P}", " ")
                .replaceAll("\\d"," ")
                .replaceAll("( )+", " ")
                .replaceAll("[\\t\\n\\r]"," ")
                .replaceAll("(\\t|\\r?\\n)+", " ")
                .toLowerCase();

        String newText="";
        String token[]=text.split(" ");
        int length=token.length;

        
//        //Removing Stop words
        for(int i=0;i<length;i++)
        {
            if(stopList.contains(token[i]))
            {
                
            } 
            else 
            {
                newText+=token[i]+" ";	
            }	
        }
        
        //creating a text file version of the pdf after filtering
        if(flag==0)
            fileName=fileName.replace(".pdf","");
        else if(flag==1)
            fileName=fileName.replace(".docx","");
        else if(flag==2)
            fileName=fileName.replace(".doc","");
        FileOutputStream textFile=new FileOutputStream(destPath+fileName+".txt");
       /* FileWriter fw=new FileWriter(path,append);
        PrintWriter pw=new PrintWriter(fw);
        pw.printf("%s"+"%n", newText);
        pw.close();
               
        */
        
        textFile.write(newText.getBytes());
        textFile.close();
        //document.close();
        //Closing the document
      //  fw.close();

    }

    /**
     * A function that scores a particular resume based on the TF-IDF scores stored in a local hash table. 
     * Individual technical, non-technical and experience related scores are written into local data structures.
     * @param resumePath Path to the resumes
     * @param flag Flag denoting technical, non-technical or experience related scoring.
     * @throws Exception 
     */
    
    public static void scoring(String resumePath, int flag) throws Exception
    {        
        File folder2 = new File(resumePath);
        File[] listOfTextFiles = folder2.listFiles();
    
        String text1;
        String[] tokens;
        FileReader ff;
        BufferedReader res;
        double calc=0;
        //List of files names in the directory
        for (int i = 0; i < listOfTextFiles.length; i++) 
        {
                calc=0;
                if (listOfTextFiles[i].isFile()) 
                {
                    ff=new FileReader(listOfTextFiles[i]);
                    res=new BufferedReader(ff);
                    while((text1=res.readLine())!=null)
                    {
                        tokens=text1.split(" ");
                        for(int j=0;j<tokens.length;++j)
                        {
                            if(flag==1)
                            {
                                if(techScoringWords.containsKey(tokens[j]))
                                {
                                    calc=calc+techScoringWords.get(tokens[j]);
                                }
                            }
                            else if(flag==2)
                            {
                                if(nontechScoringWords.containsKey(tokens[j]))
                                {
                                    calc=calc+nontechScoringWords.get(tokens[j]);
                                }                                
                            }
                            else if(flag==3)
                            {
                                if(expScoringWords.containsKey(tokens[j]))
                                {
                                    calc=calc+expScoringWords.get(tokens[j]);
                                }
                            }
                        }       
                    }
                    if(flag==1)
                        techScores.put(listOfTextFiles[i].getName(), calc);
                    else if(flag==2)
                        nontechScores.put(listOfTextFiles[i].getName(), calc);
                    else if(flag==3)
                        expScores.put(listOfTextFiles[i].getName(), calc);
                    ff.close();
                    res.close();
                } 
        }
    }
    
//   private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order)
//    {
//       List l=new LinkedList(unsortMap.entrySet());
//       Collections.sort(l, new Comparator<Map.Entry<String, Double>>(){
//
//         public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
//            return o2.getValue().compareTo(o1.getValue());
//        }});
//       Map res=new LinkedHashMap();
//       for(Iterator it=l.iterator(); it.hasNext();)
//       {
//           Map.Entry entry= (Map.Entry) it.next();
//           res.put(entry.getKey(), entry.getValue());
//       }
//       return res;
//    }
  
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByComparator(Map<K, V> map, String resultPath) throws Exception
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
            Collections.sort( list, new Comparator<Map.Entry<K, V>>()
            {
                @Override
                public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
                {
                    return ( o2.getValue() ).compareTo( o1.getValue() );
                }
            } );
        Map<K, V> result = new LinkedHashMap<>();
        String file="";
        FileWriter fw=new FileWriter(resultPath,false);
        PrintWriter pw=new PrintWriter(fw);
        for (Map.Entry<K, V> entry : list)
        {
            pw.println(entry.getKey()+" "+entry.getValue());//sortedScores.get(file));
            result.put(entry.getKey(), entry.getValue());
        }        
        pw.close();
        fw.close();
        return result;
    }

    /**
     * A function that writes the resultant scores into the file specified by the resultPath.
     * @param resumePath Path to folder containing resumes
     * @param resultPath Destination folder path
     * @throws Exception 
     */
    
    public static void writeResults(String resumePath, String resultPath) throws Exception
    {
       Map<String, Double> sortedScores;//=new Hashtable<String, Double>();
        File folder2 = new File(resumePath);
        File[] listOfTextFiles = folder2.listFiles();
        
        FileWriter fw=new FileWriter(resultPath,false);
        PrintWriter pw=new PrintWriter(fw);
        double tech=0, nontech=0,exp=0,score=0;
        String file="";
        for (int i = 0; i < listOfTextFiles.length; i++) 
        {
            file=listOfTextFiles[i].getName();
            if (listOfTextFiles[i].isFile()) 
            {
                tech=techScores.get(file)*ahpResult[0][0];
                nontech=nontechScores.get(file)*ahpResult[1][0]; 
                exp=expScores.get(file)*ahpResult[2][0];
                score=tech+nontech+exp;
                finalScores.put(file, score);
            }
        }
        sortByComparator(finalScores, resultPath);
        pw.close();
        fw.close();
    }    
}
