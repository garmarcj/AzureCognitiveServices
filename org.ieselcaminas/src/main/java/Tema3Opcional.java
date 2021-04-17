import com.google.common.io.ByteStreams;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ClassifyImageUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction;

import java.util.UUID;

public class Tema3Opcional {
    public static void main(String[] args) {

        //
        // INICIO - Autenticación
        //
        final String CLAVE_PREDICCION_CUSTOMVISION = "fc07878f469c4d5087cd72ba5ea89f22";
        final String ENDPOINT_CUSTOMVISION = "https://tema3customvision.cognitiveservices.azure.com/";
        final String ID_PROYECTO_CUSTOMVISION_FRUTAS = "7aa2d481-4a3e-47c6-92bb-1fc05c081032";
        final String NOMBRE_PUBLICACION = "Iteration1";
        final String URL_CARPETA_IMAGENES = "/home/cristian/Descargas/FIDS30/bananas/";

        CustomVisionPredictionClient clientePrediccionCustomVision = CustomVisionPredictionManager
                .authenticate(CLAVE_PREDICCION_CUSTOMVISION)
                .withEndpoint(ENDPOINT_CUSTOMVISION);
        //
        // FIN - Autenticación
        //


        String imagenAnalisis = "54.jpg";

        // load test image
        byte[] testImage = GetImage(URL_CARPETA_IMAGENES, imagenAnalisis);

   /*     byte[] testImage = new byte[0];
        File initialFile = new File("/home/cristian/Descargas/FIDS30/bananas/54.jpg");
        try {
            InputStream targetStream = new FileInputStream(initialFile);
            testImage = ByteStreams.toByteArray(targetStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        System.out.println(testImage);
        // predict
/*        ImagePrediction resultado = clientePrediccionCustomVision.predictions()
                .classifyImage(UUID.fromString(ID_PROYECTO_CUSTOMVISION_FRUTAS),NOMBRE_PUBLICACION,testImage,new ClassifyImageOptionalParameter());
                .detectImage()
                .withProjectId(UUID.fromString(ID_PROYECTO_CUSTOMVISION_FRUTAS))
                .withPublishedName(NOMBRE_PUBLICACION)
                .withImageData(testImage)
                .execute();
*/

        // Declaramos una variable imagenAnalisis y le asignamos la imagen a analizar

        ImagePrediction resultado = clientePrediccionCustomVision.predictions()
                .classifyImageUrl(UUID.fromString(ID_PROYECTO_CUSTOMVISION_FRUTAS), NOMBRE_PUBLICACION, URL_CARPETA_IMAGENES+imagenAnalisis, new ClassifyImageUrlOptionalParameter());

        for (Prediction prediccion : resultado.predictions()) {
            System.out.println(prediccion.tagName() + ": " + prediccion.probability());
        }

    }

    private static byte[] GetImage(String carpeta, String nombreFichero)
    {
        try {
            return ByteStreams.toByteArray(Tema3Opcional.class.getResourceAsStream(nombreFichero));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}