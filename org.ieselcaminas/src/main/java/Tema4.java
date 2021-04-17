import java.util.concurrent.ExecutionException;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.cognitiveservices.speech.*;

public class Tema4 {

    //
    // INICIO - Autenticación
    //

    // Constante CLAVE_SPEECH que contiene la clave de mi suscripción a Speech
    // Constante SPEECH_REGION que contiene la region en la que se encuentra alojado el servicio Speech
    final static String CLAVE_SPEECH = "78c1ecd5eb4b4a8bae0d6ff8f8d7b22a";
    final static String SPEECH_REGION = "brazilsouth";

    // Constante CLAVE_TRANSLATOR que contiene la clave de mi suscripción a Translator
    // Constante TRANSLATOR_REGION que contiene la region en la que se encuentra alojado el servicio Translator
    final static String CLAVE_TRANSLATOR = "0069b1e81d1243cfa0b60fe39f41f0f2";
    final static String TRANSLATOR_REGION = "global";

    // Constante CLAVE_TEXTANALYTICS que contiene la clave de mi suscripción a Text Analytics
    // Constante ENDPOINT_TEXTANALYTICS que contiene el ENDPOINT de mi suscripción a Text Analytics
    final static String CLAVE_TEXTANALYTICS = "967b75b01ddf42a3a54122f89cbf4b2a";
    final static String ENDPOINT_TEXTANALYTICS = "https://tema4analisisdetexto.cognitiveservices.azure.com/";

    //
    // FIN - Autenticación
    //

    public static void main(String[] args) {

        // Declaramos una variable textoReconocido que almacenará el texto hablado a través del micrófono
        String textoReconocido = null;

        System.out.println("Dime algo...");
        // Convertimos en texto escrito aquello que se diga por el micrófono
        try {
            textoReconocido = vozATexto();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Esto es lo que he escuchado... " + textoReconocido);
        // Analizamos el texto que se ha reconocido
        analisisTexto(textoReconocido);

        // Generamos la síntesis de voz con el texto que se encuentra en la variable textoHablado
        String textoHablado = "Program run has ended";
        textoAVoz(textoHablado);
        System.out.println(textoHablado);
    }

    //
    // Método estático vozATexto
    // Sintetiza en texto escrito lo que se dice por el micrófono
    //
    public static String vozATexto() throws ExecutionException, InterruptedException {
        // Creamos la configuración necesaria
        SpeechConfig configuracionSpeech = SpeechConfig.fromSubscription(CLAVE_SPEECH, SPEECH_REGION);

        // Inicializamos el cliente de SpeechRecognizer con la configuración anterior, además del idioma español
        SpeechRecognizer clienteSpeech = new SpeechRecognizer(configuracionSpeech, "es-ES");

        // Realizamos la llamada a la API para el reconocimiento de la voz
        SpeechRecognitionResult resultado = clienteSpeech.recognizeOnceAsync().get();

        return resultado.getText();
    }
    //
    // FIN - Método estático vozATexto
    //

    //
    // Método estático analisisTexto
    // Extrae características genéricas, como el sentimiento o las palabras clave, del texto que recibe como parámetro
    //
    public static void analisisTexto(String texto) {
        // Inicializamos el el cliente de TextAnalytics con la configuración necesaria
        TextAnalyticsClient clienteTextAnalytics = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential(CLAVE_TEXTANALYTICS))
                .endpoint(ENDPOINT_TEXTANALYTICS)
                .buildClient();

        // Invocamos el método de la API que nos permite analizar el sentimiento del texto pasado como parámetro
        DocumentSentiment resultado = clienteTextAnalytics.analyzeSentiment(texto, "es");

        // Imprimimos el resultado del análisis del sentimiento
        System.out.println("Opinión general... " + resultado.getSentiment());

        // Invocamos el método de la API que nos permite extraer las palabras clave del texto pasado como parámetro
        KeyPhrasesCollection palabrasClave = clienteTextAnalytics.extractKeyPhrases(texto, "es");

        System.out.print("Palabras clave... ");
        // Imprimimos el resultado de las palabras clave extraídas
        for (String palabraClave : palabrasClave) {
            System.out.print(palabraClave + ", ");
        }
        System.out.println();

        // Invocamos el método de la API que nos permite reconocer entidades del texto pasado como parámetro
        CategorizedEntityCollection entidades = clienteTextAnalytics.recognizeEntities(texto, "es");

        System.out.println("Entidades reconocidas... ");
        // Imprimimos el resultado del reconocimiento de entidades
        for (CategorizedEntity entidad : entidades) {
            System.out.println("|-> Entidad: " + entidad.getText() + " - Categoría: " + entidad.getCategory() + " , Subcategoría: " + entidad.getSubcategory());
        }
    }
    //
    // FIN - Método estático analisisTexto
    //

    //
    // Método estático textoAVoz
    // Genera la síntesis de voz con el texto que recibe como parámetro
    //
    private static void textoAVoz(String texto) {
        // Creamos la configuración necesaria
        SpeechConfig configuracionSpeech = SpeechConfig.fromSubscription(CLAVE_SPEECH, SPEECH_REGION);

        // Inicializamos el el cliente de SpeechSyntesizer con la configuración anterior
        SpeechSynthesizer clienteSpeechSyntesizer = new SpeechSynthesizer(configuracionSpeech);

        // Invocamos el método de la API que nos permite sintetizar la voz del texto pasado como parámetro
        clienteSpeechSyntesizer.SpeakText(texto);
    }
    //
    // FIN - Método estático textoAVoz
    //

}