/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
@WebServlet(urlPatterns = {"/corpusServlet"})
public class corpusServlet extends HttpServlet {

    /**
     * This takes in the folder paths that were inputted in coreProcess.html.
     * The source files are read, converted to .txt and processed using the function accessSourceFiles() in CoreProcess. 
     * TF-IDF scores are calculated and stored in local data structures.
     * After processing, the next step is to load the TF-IDF scores into a text file from the local data structures.
     * The next path in processing is to scoringServlet as all input are obtained and scoring has to begin.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */

            try
            {
                ServletContext context = request.getServletContext();
                
                String techpath=request.getParameter("techpath");
                String nontechpath=request.getParameter("nontechpath");
                String exppath=request.getParameter("exppath");
                //Access the technical source files.
                
                    String destPath=context.getRealPath("/Files/VocabList/Tech/");            
                    CoreProcess.accessSourceFiles(techpath, destPath, 1);
                    //Access the nontechnical source files
                
                    destPath=context.getRealPath("/Files/VocabList/Nontech/");
                    CoreProcess.accessSourceFiles(nontechpath, destPath, 2);
                //Access the experience source files
                
                    destPath=context.getRealPath("/Files/VocabList/Experience/");
                    CoreProcess.accessSourceFiles(exppath, destPath, 3);
                
                //Filter out common words and write to text files.
                //CoreProcess.filterWords();
                
                String techvocabpath=context.getRealPath("/Files/techVocabScore.txt");                
                CoreProcess.loadTechWords(techvocabpath);
                
                String nontechvocabpath=context.getRealPath("/Files/nontechVocabScore.txt");
                CoreProcess.loadNontechWords(nontechvocabpath);
                
                String expvocabpath=context.getRealPath("/Files/expVocabScore.txt");
                CoreProcess.loadExpWords(expvocabpath);
                
                RequestDispatcher view = request.getRequestDispatcher("/scoringServlet");
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
