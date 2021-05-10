import com.microsoft.bing.websearch.implementation.WebSearchClientImpl;
import com.microsoft.bing.websearch.models.ImageObject;
import com.microsoft.bing.websearch.models.SearchResponse;
import com.microsoft.bing.websearch.models.VideoObject;
import com.microsoft.bing.websearch.models.WebPage;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Tema7 {

    //
    // INICIO - Autenticación
    //

    // Constante CLAVE_BINGSEARCH que contiene la clave de mi suscripción a BINGSEARCH
    // Constante ENDPOINT_BINGSEARCH que contiene el ENDPOINT de mi suscripción a BINGSEARCH (no es particular, es genérico)
    private static final String CLAVE_BINGSEARCH = "0d7e4215f8fb43a9833b3afa575f36a1";
    private static final String ENDPOINT_BINGSEARCH = "https://api.bing.microsoft.com/v7.0";

    //
    // FIN - Autenticación
    //

    public static void main(String[] args) {

        System.out.print("¿Qué quieres buscar? ");
        // Declaramos una variable para leer la entrada del teclado realizada por el usuario
        Scanner teclado = new Scanner(System.in);
        String busqueda = teclado.nextLine();

        // Para crear el cliente de Bing, necesitamos utilizar la interfaz ServiceClientCredentials,
        // por lo que estamos obligados a sobreescribir el método applyCredentialsFilter
        ServiceClientCredentials credencialesCliente = new ServiceClientCredentials() {
            @Override
            public void applyCredentialsFilter(Builder builder) {
                builder.addNetworkInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = null;
                                Request original = chain.request();
                                Request.Builder requestBuilder = original.newBuilder();
                                requestBuilder.addHeader("Ocp-Apim-Subscription-Key", CLAVE_BINGSEARCH);
                                request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        }
                );
            }
        };

        // Realizamos la llamada a la API para buscar en Bing (aquí es donde utilizamos las credenciales)
        WebSearchClientImpl clienteWebSearch = new WebSearchClientImpl(ENDPOINT_BINGSEARCH, credencialesCliente);

        // Recogemos los resultados
        SearchResponse resultado = clienteWebSearch.webs().search(busqueda);

        // Recogemos la primera web de los resultados
        WebPage enlace = resultadoPrimeraWeb(resultado);

        // Recogemos la primera imagen de los resultados
        ImageObject miniatura = resultadoPrimeraImagen(resultado);

        // Recogemos el primer video de los resultados
        VideoObject video = resultadoPrimerVideo(resultado);

        // Generamos el archivo HTML con la información solicitada en el enunciado
        generarHTML(busqueda, enlace, miniatura, video);
    }

    //
    // Método estático resultadoPrimerVideo
    // A partir de los resultados de la búsqueda, devuelve un objeto VideoObject con el primer video encontrado
    //
    private static VideoObject resultadoPrimerVideo(SearchResponse resultado) {
        // Procesamos los vídeos encontrados
        if (resultado != null && resultado.videos() != null && resultado.videos().value() != null && resultado.videos().value().size() > 0)
            // nos quedamos con el primer vídeo
            return resultado.videos().value().get(0);
        else
            return null;
    }
    //
    // FIN - Método estático resultadoPrimerVideo
    //

    //
    // Método estático resultadoPrimeraImagen
    // A partir de los resultados de la búsqueda, devuelve un objeto ImageObject con la primera imagen encontrada
    //
    private static ImageObject resultadoPrimeraImagen(SearchResponse resultado) {
        // Procesamos las imágenes encontradas
        if (resultado != null && resultado.images() != null && resultado.images().value() != null && resultado.images().value().size() > 0)
            // nos quedamos con la miniatura de la primera imagen
            return resultado.images().value().get(0);
        else
            return null;
    }
    //
    // FIN - Método estático resultadoPrimeraImagen
    //

    //
    // Método estático resultadoPrimeraWeb
    // A partir de los resultados de la búsqueda, devuelve un objeto WebPage con la primera página Web encontrada
    //
    private static WebPage resultadoPrimeraWeb(SearchResponse resultado) {
        // Procesamos las páginas Web encontradas
        if (resultado != null && resultado.webPages() != null && resultado.webPages().value() != null && resultado.webPages().value().size() > 0)
            // nos quedamos con la primera página Web
            return resultado.webPages().value().get(0);
        else
            return null;
    }
    //
    // FIN - Método estático resultadoPrimeraWeb
    //

    //
    // Método estático generarHTML
    // Genera una archivo HTML a partir de la primera web, imagen y vídeo (si los hay) de la búsqueda que se pasa como parámetro
    //
    private static void generarHTML(String busqueda, WebPage enlace, ImageObject miniatura, VideoObject video) {
        FileWriter fw;
        PrintWriter pw;

        try {
            fw = new FileWriter("index.html");
            pw = new PrintWriter(fw);

            pw.println("<html>");
            pw.println("<head><title>Cristian Jorge Garcia Marcos - Azure Cognitive Services - Tema 7</title></head>");
            pw.println("<body>");

            pw.println("<p><center><h1>"+busqueda+"</h1></center></p>");

            if (miniatura != null)
                pw.println("<p><center><img src=\""+miniatura.thumbnailUrl()+"\" alt=\""+miniatura.name()+"\"></center></p>");
            else
                pw.println("<center><h3>no se ha encontrado ninguna imagen</h3></center>");

            if (enlace != null)
                pw.println("<center><a href=\""+enlace.url()+"\">"+enlace.name()+"</a></center>");
            else
                pw.println("<p><center><h3>no se ha encontrado ninguna web</h3></center></p>");

            if (video != null)
                pw.println("<p><center>"+video.embedHtml()+"</center></p>");
            else
                pw.println("<center><h3>no se ha encontrado ningún vídeo</h3></center>");

            pw.println("<script type=\"text/javascript\"");
            pw.println("id=\"bcs_js_snippet\"");
            pw.println("src=\"https://ui.customsearch.ai/api/ux/rendering-js?customConfig=f5eaa269-1dfe-4c0b-beda-fb18f8f51366&market=en-US&version=latest&q=\">");
            pw.println("</script>");

            pw.println("</body>");
            pw.println("</html>");

            pw.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //
    // FIN - Método estático generarHTML
    //

}