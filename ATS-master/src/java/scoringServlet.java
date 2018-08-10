/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(urlPatterns = {"/scoringServlet"})
public class scoringServlet extends HttpServlet {

    /**
     * This servlet is used to call the required functions in CoreProcess for scoring.
     * The scores are calculated internally and the output is displayed in HTML table format to the user.
     * This is the final part of the work-flow and the output is displayed accordingly.
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
            ServletContext context = request.getServletContext();
            out.println("<html> <head> <link type=\"text/css\" href=\"./css/materialize.css\" rel=\"stylesheet\">\n" 
                    +"<link type=\"text/css\" href=\"./css/materialize.min.css\" rel=\"stylesheet\">\n"
                    +"<meta charset=\"UTF-8\">\n" 
                    +"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> ");            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Result Page</title>");
            out.println("<style>\n" +
"                           table{\n" +
"                           margin-left : 20%;\n"+                                      
"                           margin-right : 25%;\n"+                    
"                           width : 70%;\n" +
"                           table-layout : fixed;\n"+     
"                           width : 1000px;\n"+                     
"                           }\n" +
"                           td{" +
"                               width : 400px; }\n" +                    
"                           th{" +
"                               width : 400px; }\n" +                    
"                    </style> ");
            out.println("</head>");
            out.println("<body class=\"background light-blue lighten-5\">");
            out.println("<div class=\"col s12\">\n" +
"                            <div class=\"card-panel hoverable center deep-purple lighten-5\">\n" +
"                                <div class=\"card-content purple-text\">\n" +
"                                    <span class=\"card-title pink-text\"><h5><b>Scoring Results</b></h5></span>\n" +
"                                        <h5 class='purple-text center'> \n" +
"                                            The table below shows the scores of the resumes provided, based on the corpus provided and the AHP criteria weights." +
"                                            Usually by observation, a score of 3000 or above indicates a fairly well-balanced resume. Thank you for using our application"+
"                                        </h5>   \n" +
"                                </div>\n" +
"                            </div>\n" +
"                        </div>");

            try
            {
                String resumePath=context.getRealPath("/Files/Resumes/Text/"); 
                CoreProcess.scoring(resumePath, 1);
                CoreProcess.scoring(resumePath, 2);
                CoreProcess.scoring(resumePath, 3);
                String resultPath=context.getRealPath("/Files/ResultantFiles.txt");
                CoreProcess.writeResults(resumePath, resultPath);
                out.println("<table class=\"centered bordered highlight responsive-table background grey lighten-3\" >\n" +
"                               <thead class=\"background black lighten-5 \">\n" +
"                                  <tr class=\"pink-text center\">\n" +
"                                     <th data-field=\"resumename\">Resume Name</th>\n" +
"                                     <th data-field=\"score\">Score</th>\n" +
"                                   </tr>\n" +
"                               </thead>");
                BufferedReader br=new BufferedReader(new FileReader(resultPath));
                String line;
                String[] tokens;
                out.println("<tbody>");
                while((line=br.readLine())!=null)
                {
                    tokens=line.split(" ");
                    out.println("<tr class=\"purple-text\">\n" +
"                                       <td>"+tokens[0]+"</td>\n" +
"                                       <td>"+tokens[1]+"</td>\n" +
"                                   </tr>");
                }
                out.println(" </tbody>"+
"                            </table>");
            }
            catch(Exception e)
            {
                
            }
            out.println("</body>");
            out.println("</html>");
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
