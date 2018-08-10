/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;

/**
 *
 * @author Vishnu
 * @author Krishnan Arun
 * @author Shreekrishna Prasad
 */
@WebServlet(urlPatterns = {"/AHPServlet"})
public class AHPServlet extends HttpServlet {

    /**
     * This function is entered on clicking the corresponding button in AHPPage.html. 
     * This takes in the AHP input given in the form and stores it in a local data variable.
     * The AHP function in CoreProcess is called using the inputted values.
     * If the array is consistent, proceed to resumeProcess.html.
     * Else, go back to AHPPage to re-input new values.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            double technontech=Double.parseDouble(request.getParameter("technontech"));
            double nontechexp=Double.parseDouble(request.getParameter("nontechexp"));
            double techexp=Double.parseDouble(request.getParameter("techexp"));
            double[][] arr=new double[3][3];
            arr[0][0]=arr[1][1]=arr[2][2]=1;
            arr[0][1]=technontech;
            arr[1][0]=(1/technontech);
            arr[0][2]=techexp;
            arr[2][0]=(1/techexp);
            arr[1][2]=nontechexp;
            arr[2][1]=(1/nontechexp);
            
            double[][] w=new double[3][1];
            String nextPath="";
            
            boolean res=CoreProcess.AHP(arr, w);
            if(res==true)
            {
                nextPath="/resumeProcess.html";
                RequestDispatcher view = request.getRequestDispatcher(nextPath);
                view.forward(request, response);                 
            }
            else
            {
                out.println("<html> <head> <link type=\"text/css\" href=\"./css/materialize.css\" rel=\"stylesheet\">\n" 
                        +"<link type=\"text/css\" href=\"./css/materialize.min.css\" rel=\"stylesheet\">\n"
                        +"<meta charset=\"UTF-8\">\n" 
                        +"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> ");
                out.println("<title> Error Page </title> </head> ");
                out.println("<body class=\"background light-blue lighten-5\">");
                out.println("<h3 class=\"brown-text center\"> THIS IS AN ERROR PAGE. </h3>");
                out.println("<div class=\"row\">\n" +
"            <div class=\"col s12\">\n" +
"                <div class=\"card hoverable center deep-purple lighten-5\">\n" +
"                    <div class=\"card-content purple-text\">\n" +
"                           <span class=\"card-title pink-text\"> <b> Inconsistencies in the AHP matrix. </b> </span>" +                       
"                            <h5> \n" +
"                                You are seeing this page because you have entered an inconsistent matrix for the AHP input. \n" +
"                                This is usually caused by transitive inconsistencies in the given input. \n" +
"                            </h5>\n" +
"                            <h5>\n" +
"                                For instance, if you had entered technical as more important than non-technical criteria and non-technical criteria as more important than experience, then it is required that technical be more important than experience \n" +
"                                Such consistencies are automatically checked by our process so that your job specification makes logical sense. \n" +
"                                Now, please click the below button to go back to the AHP page and re-enter your input. Thank you. \n" +
"                            </h5>\n"+
"                    </div>" +
"            </div>");
            out.println("<div class=\"row container\">\n" +
"        <form action=\"AHPPage.html\" method=\"post\" class=\"col s12\">"+
"           <button class=\"btn waves-effect waves-light right green accent-4\" type=\"submit\" name=\"action\"> Go back \n" +
"                <i class=\"material-icons right\"></i>\n" +
"            </button>"+
"        </form> </div> </body> </html>");
                
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
