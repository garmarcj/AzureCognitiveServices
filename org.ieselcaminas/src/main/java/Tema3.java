import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.GenerateThumbnailOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.Line;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OcrDetectionLanguage;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadHeaders;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadResult;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

public class Tema3 {
    public static void main(String[] args) {

        //
        // INICIO - Autenticación
        //

        // Constante URL_CARPETA_IMAGENES que contiene el URL de la carpeta donde se encuentran las imagenes a analizar
        final String URL_CARPETA_IMAGENES = "https://moderatorsampleimages.blob.core.windows.net/samples/";
        // Constante ENDPOINT_COMPUTERVISION, que contiene el ENDPOINT de mi suscripción a ComputerVision
        final String ENDPOINT_COMPUTERVISION = "https://tema3computervision.cognitiveservices.azure.com/";

        // Constante CLAVE_SUSCRIPCION_COMPUTERVISION que contiene la clave de mi suscripción a ComputerVision
        final String CLAVE_SUSCRIPCION_COMPUTERVISION = "438c0e991ea748eaad58877d93f5ec82";

        // Inicializamos un objeto ComputerVisionClient (que es el cliente de ComputerVision) para realizar la autenticación
        ComputerVisionClient clienteComputerVision = ComputerVisionManager.authenticate(CLAVE_SUSCRIPCION_COMPUTERVISION).withEndpoint(ENDPOINT_COMPUTERVISION);

        //
        // FIN - Autenticación
        //

        // Declaramos una variable imagenAnalisis y le asignamos la imagen a analizar
        String imagenAnalisis = "sample2.jpg";

        System.out.println("Miniaturas...");
        // Creamos 2 miniaturas
        generarMiniatura(clienteComputerVision,URL_CARPETA_IMAGENES,imagenAnalisis,100, 50, "miniatura1.jpg");
        generarMiniatura(clienteComputerVision,URL_CARPETA_IMAGENES,imagenAnalisis,50, 100, "miniatura2.jpg");

        System.out.println("Descripción de la imagen...");
        // Analizamos la imagen
        Map <String,List<String>> descriptores = analizarImagen(clienteComputerVision,URL_CARPETA_IMAGENES,imagenAnalisis);

        System.out.println("OCR...");
        // Leemos el texto de la imagen (en el caso que lo haya)
        String ocr = procesarTexto(clienteComputerVision, URL_CARPETA_IMAGENES, imagenAnalisis);

        // Generamos el archivo HTML con la información solicitada en el enunciado
        generarHTML(clienteComputerVision, URL_CARPETA_IMAGENES, imagenAnalisis, descriptores, ocr);
        System.out.println("Archivo HTML generado correctamente");
    }

    //
    // Método estático generarHTML
    // Genera una archivo HTML a partir de los descriptores y del texto (si lo hay) de la imagen que se pasa como parámetro
    //
    private static void generarHTML(ComputerVisionClient clienteComputerVision, String imagenURL, String imagenNombre, Map<String,List<String>> descriptores, String ocr) {
        FileWriter fw;
        PrintWriter pw;

        try {

            fw = new FileWriter("index.html");
            pw = new PrintWriter(fw);

            pw.println("<html>");
            pw.println("<head><title>Cristian Jorge Garcia Marcos - Azure Cognitive Services - Tema 3</title></head>");
            pw.println("<body bgcolor=\""+descriptores.get("Color").get(0)+"\">");

            pw.println("<center><h1><font color=\"purple\">"+descriptores.get("Descripción").get(0)+"</font></h1></center>");

            pw.println("<p><center><img src=\""+imagenURL+imagenNombre+"\"></center></p>");
            pw.println("<p><img src=\"miniatura1.jpg\"></p>");
            pw.println("<p><img src=\"miniatura2.jpg\"></p>");

            pw.println("<center><h3>");
            List<String> etiquetas = descriptores.get("Etiqueta");
            for (String etiqueta : etiquetas) {
                pw.println(etiqueta + " ");
            }
            pw.println("</h3></center>");

            pw.println("<center><h2>" + ocr + "</h2></center>");

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

    //
    // Método estático generarMiniatura
    // Genera una miniatura de una imagen a partir del nombre del archivo con la imagen y el URL que lo contiene
    //
    private static void generarMiniatura(ComputerVisionClient clienteComputerVision, String imagenURL, String imagenNombre, int ancho, int alto, String nombreMiniatura) {
        // Hacemos casting del cliente de ComputerVision (que es de tipo CommputerVisionClient) a ComputerVisionImpl
        // para poder acceder a los métodos que necesitamos de la API para generar la miniatura
        ComputerVisionImpl clienteComputerVisionImpl = (ComputerVisionImpl) clienteComputerVision.computerVision();

        // Invocamos el método de la API que nos permite generar la miniatura
        InputStream miniatura = clienteComputerVisionImpl.generateThumbnail(ancho,alto, imagenURL+imagenNombre,new GenerateThumbnailOptionalParameter());

        // Creamos un archivo con la miniatura
        try {
            crearFichero(miniatura, nombreMiniatura);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //
    // FIN - Método estático generarMiniatura
    //

    //
    // Método estático crearFichero
    // Crea un fichero que tiene el nombre que recibe en el parámetro miniatura y con el contenido que recibe en el parámetro is
    //
    public static void crearFichero(InputStream is, String miniatura) throws IOException {
        FileOutputStream fop;
        File fichero;

        fichero = new File(miniatura);
        fop = new FileOutputStream(fichero);
        is.transferTo(fop);
        fop.close();
        is.close();
    }
    //
    // FIN - Método estático crearFichero
    //

    //
    // Método estático analizarImagen
    // Analiza las características de una imagen a partir del nombre del archivo con la imagen y el URL que lo contiene
    //
    public static Map<String, List<String>> analizarImagen(ComputerVisionClient clienteComputerVision, String imagenURL, String imagenNombre) {
        Map<String,List<String>> mapaCaracteristicas = new HashMap<>();

        // Creamos una lista con las características y añadimos aquellas que queremos obtener de cada cara
        List<VisualFeatureTypes> caracteristicas = new ArrayList<>();
        caracteristicas.add(VisualFeatureTypes.DESCRIPTION);
        caracteristicas.add(VisualFeatureTypes.TAGS);
        caracteristicas.add(VisualFeatureTypes.COLOR);

        // Invocamos el método de la API que nos permite analizar la imagen
        ImageAnalysis resultado = clienteComputerVision.computerVision().analyzeImage().withUrl(imagenURL+imagenNombre).withVisualFeatures(caracteristicas).execute();

        // Procesamos el resultado
        // .descripción
        mapaCaracteristicas.put("Descripción", Collections.singletonList(resultado.description().captions().get(0).text()));

        // .etiquetas
        List<String> etiquetas = new ArrayList<>();
        for (ImageTag tag : resultado.tags()) {
            etiquetas.add(tag.name());
        }
        mapaCaracteristicas.put("Etiqueta", etiquetas);

        // .color
        mapaCaracteristicas.put("Color", Collections.singletonList(resultado.color().accentColor()));

        return mapaCaracteristicas;
    }
    //
    // FIN - Método estático analizarImagen
    //


    //
    // Método estático leerTexto
    // Procesa el texto de una imagen, en el caso que lo haya
    //
    private static String procesarTexto(ComputerVisionClient clienteComputerVision, String imagenURL, String imagenNombre) {
        // Hacemos casting del cliente de ComputerVision (que es de tipo CommputerVisionClient) a ComputerVisionImpl
        // para poder acceder a los métodos que necesitamos de la API para leer texto
        ComputerVisionImpl clienteComputerVisionImpl = (ComputerVisionImpl) clienteComputerVision.computerVision();

        // Invocamos el método de la API que nos permite leer el texto de la imagen
        ReadHeaders operacionOCR = clienteComputerVisionImpl.readWithServiceResponseAsync(imagenURL+imagenNombre, OcrDetectionLanguage.ES)
                .toBlocking()
                .single()
                .headers();

        // Obtenemos el localizador de la operacion de lectura del texto
        String localizador = operacionOCR.operationLocation();

        // Procesamos el resultado de la operación de lectura del texto
        try {
            return imprimirLetras(clienteComputerVisionImpl,localizador);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "La imagen no contiene texto";
    }
    //
    // FIN - Método estático leerTexto
    //

    //
    // Método estático imprimirLetras
    // Imprime el texto de una imagen, en el caso que lo haya
    //
    private static String imprimirLetras(ComputerVision vision, String localizador) throws InterruptedException {
        // Extraemos el identificador de la operación de lectura a partir del localizador
        String operationId = extraerOperationIdDesdeLocalizador(localizador);

        // Esperamos a que la operación de lectura acabe
        boolean pollForResult = true;
        ReadOperationResult readResults = null;
        while (pollForResult) {
            Thread.sleep(1000);
            readResults = vision.getReadResult(UUID.fromString(operationId));
            if (readResults != null) {
                OperationStatusCodes status = readResults.status();
                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false;
                }
            }
        }

        // Imprimimos el resultado de la lectura
        StringBuilder builder = new StringBuilder();
        for (ReadResult pagina : readResults.analyzeResult().readResults()) {
            for (Line linea : pagina.lines()) {
                builder.append(linea.text());
                builder.append("\n");
            }
        }
        return builder.toString();
    }
    //
    // FIN - Método estático imprimirLetras
    //

    //
    // Método estático extraerOperationIdDesdeLocalizador
    // Extrae el OperationId a partir del localizador que recibe como parámetro
    //
    private static String extraerOperationIdDesdeLocalizador(String localizador) {
        if (localizador != null && !localizador.isEmpty()) {
            String[] splits = localizador.split("/");

            if (splits.length > 0) {
                return splits[splits.length - 1];
            }
        }
        throw new IllegalStateException("Algo ha ido mal. No se puede extraer el operationID del localizador");
    }
    //
    // FIN - Método estático extraerOperationIdDesdeLocalizador
    //

}