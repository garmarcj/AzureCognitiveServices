package org.ieselcaminas.azurecognitiveservices;

import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPI;
import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPIManager;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupPersonsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectWithUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.FaceAttributeType;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.IdentifyResult;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.Person;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatus;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatusType;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.VerifyResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Tema2 {
    enum Emociones {ENOJO, DESPRECIO, ASCO, TEMOR, FELICIDAD, NEUTRAL, TRISTEZA, SORPRESA}
    public static void main(String[] args) {

        //
        // INICIO - Autenticación
        //

        // Constante URL_CARPETA_IMAGENES que contiene el URL de la carpeta donde se encuentran las imagenes a analizar
        final String URL_CARPETA_IMAGENES = "https://csdx.blob.core.windows.net/resources/Face/Images/";

        // Constante ID_GRUPO_PERSONAS que contiene el nombre del grupo de personas para el servicio de identificación
        final String ID_GRUPO_PERSONAS = "mi-familia";

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

        System.out.println("Verificando caras...");
        // Declaramos dos variables y les asignamos las imágenes a detectar
        String imagenFuente1 = "Family1-Dad1.jpg";
        String imagenFuente2 = "Family1-Dad2.jpg";
        // Verificamos las caras
        boolean sonCarasIguales = verificar2Caras(clienteFace, URL_CARPETA_IMAGENES, imagenFuente1, imagenFuente2);
        // Si son caras iguales, identificaremos las caras. Si no lo son, el programa finaliza
        if (sonCarasIguales) {
            System.out.println("Las dos caras pertenecen a la misma persona");
            System.out.println();
            // Identificamos las caras
            System.out.println("Identificando caras...");
            String identificacionCaras = identificarCaras(clienteFace, URL_CARPETA_IMAGENES, ID_GRUPO_PERSONAS);
            System.out.println(identificacionCaras);
        }
        else {
            System.out.println("Las dos caras no pertenecen a la misma persona");
        }
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

        // Almacenaremos la información requerida de cada cara que se ha detectado
        String resultado = "";

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
            resultado +=  "Cara{" +
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
        return resultado;
    }
    //
    // FIN - Método estático detectarCaras
    //

    //
    // Método estático verificarCaras
    // Dadas 2 fotos con 1 única cara, verifica si corresponden o no a la misma persona
    //
    public static boolean verificar2Caras(FaceAPI clienteFace, String imagenURL, String imagen1, String imagen2) {
        // Almacenamos las caras de las imágenes, que deben ser detectadas antes de realizar la verificación
        List<DetectedFace> imagen1ID = clienteFace.faces().detectWithUrl(imagenURL+imagen1,new DetectWithUrlOptionalParameter().withReturnFaceId(true));
        List<DetectedFace> imagen2ID = clienteFace.faces().detectWithUrl(imagenURL+imagen2,new DetectWithUrlOptionalParameter().withReturnFaceId(true));

        // Verificamos caras de las imágenes
        VerifyResult verificacion = clienteFace.faces().verifyFaceToFace(imagen1ID.get(0).faceId(),imagen2ID.get(0).faceId());

        // Devolvemos la similitud o no similitud de ambas caras
        return (verificacion.isIdentical() ? true : false);
    }
    //
    // FIN - Método estático verificarCaras
    //

    //
    // Método estático identificarCaras
    // Para identificar una cara, se crea un grupo de personas y se utiliza una lista de caras detectadas
    // La lista con caras similares se asigna a una persona del grupo
    // para entrenar a la IA y que esta pueda identificar imágenes posteriores de esa persona
    //
    public static String identificarCaras(FaceAPI clienteFace, String imagenURL, String grupoPersonasID) {
        // Almacenaremos la información de las caras que se han identificado
        String resultado = "";

        // Creamos un diccionario para almacenar las caras
        Map<String, String[]> listaCaras = new HashMap<>();
        listaCaras.put("Padre", new String[] { "Family1-Dad1.jpg", "Family1-Dad2.jpg" });
        listaCaras.put("Madre", new String[] { "Family1-Mom1.jpg", "Family1-Mom2.jpg" });

        // Eliminamos el ID del grupo previamente (por si acaso existiera)
        try {
            clienteFace.personGroups().delete(grupoPersonasID);
        } catch (Exception e) {}

        System.out.println("Paso 1. Creando el grupo " + grupoPersonasID + "...");
        // Creamos el grupo al que se asignarán las imágenes
        clienteFace.personGroups().create(grupoPersonasID,new CreatePersonGroupsOptionalParameter().withName(grupoPersonasID));

        // Agrupamos las caras. Cara array de caras similares se agrupará en una única persona del grupo
        for (String nombrePersona : listaCaras.keySet()) {
            System.out.println("|-> Añadiendo a " + nombrePersona + " al grupo " + grupoPersonasID + "...");
            // Asociamos el nombre del miembro de la familia con un ID
            Person person = clienteFace.personGroupPersons().create(grupoPersonasID,
                    new CreatePersonGroupPersonsOptionalParameter().withName(nombrePersona));

            for (String imagenPersona : listaCaras.get(nombrePersona)) {
                // Añadimos cada imagen del array a la persona del grupo
                clienteFace.personGroupPersons().addPersonFaceFromUrl(grupoPersonasID, person.personId(), imagenURL + imagenPersona, null);
            }
        }

        // Entrenamos el grupo
        System.out.println();
        System.out.println("Paso 2. Entrenando al grupo " + grupoPersonasID + "...");
        clienteFace.personGroups().train(grupoPersonasID);

        // Esperamos hasta que el entrenamiento se haya completado
        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) { e.printStackTrace(); }

            TrainingStatus status = clienteFace.personGroups().getTrainingStatus(grupoPersonasID);
            if (status.status() == TrainingStatusType.SUCCEEDED) {
                break;
            }
        }

        // Detectamos las caras de una imagen de grupo
        String fotoGrupo = "identification1.jpg";
        List<DetectedFace> listaCarasDetectadas = clienteFace.faces().detectWithUrl(imagenURL+fotoGrupo,new DetectWithUrlOptionalParameter().withReturnFaceId(true));
        // Creamos una lista que contiene únicamente los IDs de las caras, que necesitamos para invocar al método identify
        List<UUID> listaCarasDetectadasUuid = new ArrayList<>();
        for (DetectedFace face : listaCarasDetectadas) {
            listaCarasDetectadasUuid.add(face.faceId());
        }
        // Invocamos el método identify de la API que nos permite identificar las caras detectadas dentro del grupo
        List<IdentifyResult> resultadoIdentificacion = clienteFace.faces().identify(grupoPersonasID, listaCarasDetectadasUuid, null);

        // Devolvemos cada persona del grupo identificada
        System.out.println("Personas del grupo identificadas en la foto " + fotoGrupo + ": ");
        for (IdentifyResult personaIdentificada : resultadoIdentificacion) {
            if (!personaIdentificada.candidates().isEmpty()) {
                Person persona = clienteFace.personGroupPersons().get(grupoPersonasID,personaIdentificada.candidates().get(0).personId());
                resultado += persona.name()+ " ";
            }
        }
        return resultado;
    }
    //
    // FIN - Método estático identificarCaras
    //
}