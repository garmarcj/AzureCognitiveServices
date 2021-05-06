import com.microsoft.azure.cognitiveservices.language.luis.runtime.*;
import com.microsoft.azure.cognitiveservices.language.luis.runtime.models.*;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.*;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.models.*;

import java.util.Scanner;
import java.util.UUID;

public class Tema5 {

    //
    // INICIO - Autenticación
    //

    // Constante CLAVE_LUIS que contiene la clave de mi suscripción a LUIS
    // Constante APP_ID que contiene el identificador de la aplicación "Comunicación" creada manualmente en LUIS
    private static String CLAVE_LUIS = "5db9f604ec0747bcaeb82d96ec5646c6";
    private static UUID APP_ID = UUID.fromString("36ae2521-bda5-4515-b323-f3a1302ea3c4");

    // Constante CLAVE_RUNTIME que contiene la clave de mi suscripción al RUNTIME de QNAMAKER
    // Constante ENDPOINT_RUNTIME que contiene el ENDPOINT de mi suscripción al RUNTIME de QNAMAKER
    // Constante KB_ID que contiene el identificador de la aplicación "Chitchat" creada manualmente en QNAMAKER
    private static String CLAVE_RUNTIME = "bd655407-f0ca-4fb1-b649-b3c3f1b7ee0c";
    private static String ENDPOINT_RUNTIME = "https://tema5qnamaker.azurewebsites.net";
    private static String KB_ID = "66c939d9-221e-4efb-9f79-10a0a8eddd55";

    //
    // FIN - Autenticación
    //

    public static void main(String[] args) {

        // Inicializamos el cliente de LuisRuntime con la configuración del ENDPOINT y la clave de LUIS
        LuisRuntimeAPI clienteLUIS = LuisRuntimeManager.authenticate(EndpointAPI.EUROPE_WEST,CLAVE_LUIS);

        // Declaramos una variable para leer las entradas del teclado realizadas por el usuario
        Scanner lectura =new Scanner(System.in);

        // Bucle de conversación
        boolean salir = false;
        do {
            // Leemos la pregunta
            System.out.println("\n¿Qué quieres saber?");
            String pregunta = lectura.nextLine();

            // Realizamos la llamada a la API para obtener las predicciones de respuesta a nuestra pregunta
            LuisResult resultado = clienteLUIS.predictions().resolve()
                    .withAppId(APP_ID.toString())
                    .withQuery(pregunta)
                    .execute();

            // Recogemos únicamente la predicción con mayor peso e imprimimos la intención y sus entidades asociadas
            IntentModel resultadoMayorPeso = resultado.topScoringIntent();
            if (resultadoMayorPeso != null) {
                System.out.println("La intención es... " + resultadoMayorPeso.intent());
                if (resultado.entities() != null && resultado.entities().size() > 0) {
                    System.out.print("Y las entidades encontradas son... ");
                    for (EntityModel entityModel : resultado.entities()) {
                        System.out.print(entityModel.entity() + " ");
                    }
                    System.out.println();
                }
                if (resultadoMayorPeso.intent().equals("Salir"))
                    salir = true;
                else if (resultadoMayorPeso.intent().equals("None")) {
                        // Inicializamos el cliente de QnaMakerRuntime con la configuración del ENDPOINT y la clave del RUNTIME de QNAMAKER
                        QnAMakerRuntimeClient clienteQnaMakerRuntime = QnAMakerRuntimeManager.authenticate(CLAVE_RUNTIME).withRuntimeEndpoint(ENDPOINT_RUNTIME);

                        // Invocamos a la API para obtener la lista de respuestas
                        QnASearchResultList respuestas = clienteQnaMakerRuntime.runtimes().generateAnswer(KB_ID, (new QueryDTO()).withQuestion(pregunta));

                        // Mostramos las respuestas
                        for (QnASearchResult respuesta : respuestas.answers()) {
                            System.out.println(respuesta.answer());
                        }
                }

            } else {
                System.out.println("No se encontró ninguna intención.");
            }
        } while (!salir);
    }

}