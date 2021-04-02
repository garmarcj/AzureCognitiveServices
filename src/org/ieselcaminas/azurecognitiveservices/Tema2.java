package org.ieselcaminas.azurecognitiveservices;

import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPI;
import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPIManager;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectWithUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.FaceAttributeType;
import java.util.*;

public class Tema2 {
    enum Emociones {ENOJO, DESPRECIO, ASCO, TEMOR, FELICIDAD, NEUTRAL, TRISTEZA, SORPRESA}
    public static void main(String[] args) {

        //
        // INICIO - Autenticación
        //

        // Constante URL_CARPETA_IMAGENES que contiene el URL de la carpeta donde se encuentran las imagenes a analizar
        final String URL_CARPETA_IMAGENES = "https://csdx.blob.core.windows.net/resources/Face/Images/";

        // Para el servicio Face, no es necesario utilizar el ENDPOINT, pero aquí lo dejamos para usos posteriores
        final String ENDPOINT_FACE = "https://tema2face.cognitiveservices.azure.com/";

        // Variable que contiene la region de mi suscripción a Face
        AzureRegions miRegion = AzureRegions.BRAZILSOUTH;

        // Constante CLAVE_SUSCRIPCION_FACE que contiene la clave de mi suscripción a Face
        final String CLAVE_SUSCRIPCION_FACE = "576497e74edd4cddb12e789f8ab3badf";

        // Inicializamos un objeto FaceAPI (que es el cliente de Face) para realizar la autenticación
        FaceAPI clienteFace = FaceAPIManager.authenticate(miRegion,CLAVE_SUSCRIPCION_FACE);

        //
        // FIN - Autenticación
        //

        System.out.println("Detectando caras...");

        // Declaramos una variable imagenAnalisis y le asignamos la imagen a analizar
        String imagenAnalisis = "detection5.jpg";

        // Detectamos las caras
        String informacionCaras = detectarCaras(clienteFace, URL_CARPETA_IMAGENES, imagenAnalisis);

        // Mostramos la información de las caras
        System.out.println(informacionCaras);
    }

    //
    // Método estático detectarCaras
    // Detecta la cara (o caras) a partir del nombre del archivo con la imagen y el URL que lo contiene
    //
    public static String detectarCaras(FaceAPI clienteFace, String imagenURL, String imagenNombre) {
        // Creamos una lista con los tipos de atributos y añadimos aquellos que queremos obtener de cada cara
        List<FaceAttributeType> atributos = new ArrayList<>();
        atributos.add(FaceAttributeType.AGE);
        atributos.add(FaceAttributeType.EMOTION);

        // Creamos una lista con las caras detectadas
        List<DetectedFace> listaCarasDetectadas = clienteFace.faces().detectWithUrl(imagenURL+imagenNombre,new DetectWithUrlOptionalParameter().withReturnFaceAttributes(atributos));

        // Obtenemos la información requerida de cada cara que se ha detectado
        String informacionCara = "";

        for (DetectedFace cara : listaCarasDetectadas) {

            // Creamos un mapa con todas las emociones y añadimos el valor estimado correspondiente de cada una de ellas
            TreeMap<Emociones,Double> valorEmociones = new TreeMap<>();
            valorEmociones.put(Emociones.ENOJO,cara.faceAttributes().emotion().anger());
            valorEmociones.put(Emociones.DESPRECIO,cara.faceAttributes().emotion().contempt());
            valorEmociones.put(Emociones.ASCO,cara.faceAttributes().emotion().disgust());
            valorEmociones.put(Emociones.TEMOR,cara.faceAttributes().emotion().fear());
            valorEmociones.put(Emociones.FELICIDAD,cara.faceAttributes().emotion().happiness());
            valorEmociones.put(Emociones.NEUTRAL,cara.faceAttributes().emotion().neutral());
            valorEmociones.put(Emociones.TRISTEZA,cara.faceAttributes().emotion().sadness());
            valorEmociones.put(Emociones.SORPRESA,cara.faceAttributes().emotion().surprise());

            // Almacenamos la emoción de la cara cuyo valor sea más significativo de entre todas las emociones detectadas
            Emociones emocionMaxima = Collections.max(valorEmociones.entrySet(), Map.Entry.comparingByValue()).getKey();

            // Preparamos la información requerida de la cara
            informacionCara +=  "Cara{" +
                    "id='" + cara.faceId() + '\'' +
                    ", ancho del rectángulo='" + cara.faceRectangle().width() + '\'' +
                    ", alto del rectángulo='" + cara.faceRectangle().height() + '\'' +
                    ", coordenada arriba del rectángulo='" + cara.faceRectangle().top() + '\'' +
                    ", coordenada izquierda del rectángulo='" + cara.faceRectangle().left() + '\'' +
                    ", edad estimada='" + cara.faceAttributes().age()+ '\'' +
                    ", emoción='" + emocionMaxima.toString()+
                    "'}" + "\n";
        }

        // Devolvemos la información requerida de todas las caras
        return informacionCara;
    }
}