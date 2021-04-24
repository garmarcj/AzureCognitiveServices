import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.google.gson.*;
import com.microsoft.cognitiveservices.speech.*;
import okhttp3.*;

public class Tema4Opcional {

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
        String textoTraducido = null;

        System.out.println("Dime algo...");
        // Convertimos en texto escrito aquello que se diga por el micrófono
        try {
            textoReconocido = vozATexto();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Esto es lo que he escuchado... " + textoReconocido);
        // Analizamos el texto que se ha reconocido
        analizarTexto(textoReconocido);

        // Traducimos el texto que se ha reconocido
        try {
            textoTraducido = traducirTexto(textoReconocido);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Aquí está la traducción al inglés y al francés... \n" + textoTraducido);

        // Generamos la síntesis de voz con el texto que se encuentra en la variable textoHablado
        String textoHablado = "La ejecución del programa ha finalizado";
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
    // Método estático analizarTexto
    // Extrae características genéricas, como el sentimiento o las palabras clave, del texto que recibe como parámetro
    //
    public static void analizarTexto(String texto) {
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
    // FIN - Método estático analizarTexto
    //

    //
    // Método estático textoAVoz
    // Genera la síntesis de voz con el texto que recibe como parámetro
    //
    public static void textoAVoz(String texto) {
        // Creamos la configuración necesaria
        SpeechConfig configuracionSpeech = SpeechConfig.fromSubscription(CLAVE_SPEECH, SPEECH_REGION);
        configuracionSpeech.setSpeechSynthesisLanguage("es-ES");

        // Inicializamos el cliente de SpeechSyntesizer con la configuración anterior
        SpeechSynthesizer clienteSpeechSyntesizer = new SpeechSynthesizer(configuracionSpeech);

        // Invocamos el método de la API que nos permite sintetizar la voz del texto pasado como parámetro
        clienteSpeechSyntesizer.SpeakText(texto);
    }
    //
    // FIN - Método estático textoAVoz
    //

    //
    // Método estático traducirTexto
    // Traduce al inglés y al francés el texto en castellano que recibe como parámetro
    //
    public static String traducirTexto(String texto) throws IOException {
        // Preparamos la petición con la clase HttpUrl
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.cognitive.microsofttranslator.com")
                .addPathSegment("/translate")
                .addQueryParameter("api-version", "3.0")
                .addQueryParameter("from", "es")
                .addQueryParameter("to", "en")
                .addQueryParameter("to", "fr")
                .build();

        // Instanciamos el cliente de OkHttpClient
        OkHttpClient clienteOkHttpClient = new OkHttpClient();

        // Preparamos la petición POST
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "[{\"Text\": \""+texto+"\"}]");
        Request request = new Request.Builder().url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", CLAVE_TRANSLATOR)
                .addHeader("Ocp-Apim-Subscription-Region", TRANSLATOR_REGION)
                .addHeader("Content-type", "application/json")
                .build();

        // Realizamos la petición y recuperamos la respuesta
        Response respuesta = clienteOkHttpClient.newCall(request).execute();

        // Embellecemos la respuesta
        JsonParser parser = new JsonParser();
        StringBuilder resultado = new StringBuilder();

        if (respuesta.body() != null) {
            JsonElement json = parser.parse(respuesta.body().string());

            JsonArray jArrayResultado = json.getAsJsonArray();
            JsonElement jsonTraducciones = jArrayResultado.get(0);
            JsonObject jsonIdioma = (JsonObject) jsonTraducciones;

            JsonArray jArrayResultadoInterno = (JsonArray) jsonIdioma.get("translations");
            for (JsonElement jsonTraduccionesInterno : jArrayResultadoInterno) {
                JsonObject jsonIdiomaInterno = (JsonObject) jsonTraduccionesInterno;
                resultado.append("|-> Idioma: ").append(jsonIdiomaInterno.get("to")).append(", Traducción: ").append(jsonIdiomaInterno.get("text").toString()).append("\n");
            }
        }
        return resultado.toString();
    }
    //
    // FIN - Método estático traducirTexto
    //

}