/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Vishnu
 * @author Krishnan Arun
 * @author Shreekrishna Prasad
 */
@WebServlet(name = "resumeServlet", urlPatterns = {"/resumeServlet"})
public class resumeServlet extends HttpServlet {

    /**
     * This servlet is entered from resumeProcess.html.
     * The stop words are loaded from the server file to a local data structure, a hash-table.
     * It takes in the resume folder path and passes it as a parameter to CoreProcess function processResumes().
     * The resumes are read, converted to .txt and stored on the server for further processing.
     * Once that is done, proceed to corpusProcess.html.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    public static void removeWhiteSpace (File IBFolder) {
    // For clarification:
    // File IBFolder = new File("path/containing/images/folder/here");
    String oldName;
    String newName;
    String temp;
    for (File old : IBFolder.listFiles())
    {
        if(old.isDirectory())
            removeWhiteSpace(old);
        else
        {
            oldName=old.getName();
            if (!oldName.contains(" "))
                continue;
            newName=oldName.replaceAll(" ", "");
            old.renameTo(new File(newName));
        }
    }
    }    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) 
        {
            /* TODO output your page here. You may use following sample code. */
            String resumePath=request.getParameter("resumepath");
            File fileresumepath=new File(resumePath);
            removeWhiteSpace(fileresumepath);
            try
            {
                ServletContext context = request.getServletContext();
                String file=context.getRealPath("/Files/listOfFiles.txt");   
                boolean append=false;
                
                CoreProcess.loadFiles(resumePath, file);
                String readText;
        //      reading each resume and calling the readFile function
                String stopPath=context.getRealPath("/Files/stop words.txt"); 
                CoreProcess.loadStopWords(stopPath);
                String destPath=context.getRealPath("/Files/Resumes/Text/"); 
                CoreProcess.processResumes(resumePath, destPath);
//                out.println("<html> <head> <link type=\"text/css\" href=\"./css/materialize.css\" rel=\"stylesheet\">\n" 
//                        +"<link type=\"text/css\" href=\"./css/materialize.min.css\" rel=\"stylesheet\">\n"
//                        +"<meta charset=\"UTF-8\">\n" 
//                        +"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> ");
//                out.println("<h5 class=\"purple-text center\">"+ret+" </h5> <head> </html>");
                RequestDispatcher view = request.getRequestDispatcher("/corpusProcess.html");
                view.forward(request, response);
                

            }
            catch(Exception e)
            {
                out.println("<html> <head> <link type=\"text/css\" href=\"./css/materialize.css\" rel=\"stylesheet\">\n" 
                        +"<link type=\"text/css\" href=\"./css/materialize.min.css\" rel=\"stylesheet\">\n"
                        +"<meta charset=\"UTF-8\">\n" 
                        +"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> ");
                out.println("<h5 class=\"purple-text center\">"+e.toString()+" </h5> <head> </html>");
                
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
