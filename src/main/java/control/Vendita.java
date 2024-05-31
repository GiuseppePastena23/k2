import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import model.ProductBean;
import model.ProductModel;

@WebServlet("/Vendita")
public class Vendita extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Vendita() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductBean product = new ProductBean();
        product.setEmail((String) request.getSession().getAttribute("email"));

        String UPLOAD_DIRECTORY = request.getServletContext().getRealPath("/") + "img/productIMG/";

        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory())
                        .parseRequest(new ServletRequestContext(request));

                for (FileItem item : multiparts) {
                    if (!item.isFormField()) {
                        String name = new File(item.getName()).getName();
                        item.write(new File(UPLOAD_DIRECTORY + File.separator + name));
                        product.setImmagine(name);
                    } else {
                        String sanitizedValue = escapeHtml(item.getString());

                        if (item.getFieldName().equals("nome")) {
                            product.setNome(sanitizedValue);
                        } else if (item.getFieldName().equals("prezzo")) {
                            double prezzo = Double.parseDouble(sanitizedValue);
                            product.setPrezzo(prezzo);
                        } else if (item.getFieldName().equals("spedizione")) {
                            double spedizione = Double.parseDouble(sanitizedValue);
                            product.setSpedizione(spedizione);
                        } else if (item.getFieldName().equals("tipologia")) {
                            product.setTipologia(sanitizedValue);
                        } else if (item.getFieldName().equals("tag")) {
                            product.setTag(sanitizedValue);
                        } else if (item.getFieldName().equals("descrizione")) {
                            product.setDescrizione(sanitizedValue);
                        }
                    }
                }

                request.setAttribute("message", "File Uploaded Successfully");
            } catch (Exception ex) {
                // Gestisci l'eccezione appropriatamente
            }
        } else {
            request.setAttribute("message", "Sorry this Servlet only handles file upload request");
        }

        ProductModel model = new ProductModel();
        try {
            model.doSave(product);
        } catch (SQLException e) {
            // Gestisci l'eccezione appropriatamente
            e.printStackTrace();
        }

        request.getSession().setAttribute("refreshProduct", true);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // Funzione per convertire i caratteri speciali in entità HTML
    protected String escapeHtml(String input) {
        StringBuilder builder = new StringBuilder();

        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                default:
                    builder.append(c);
            }
        }

        return builder.toString();
    }
}

