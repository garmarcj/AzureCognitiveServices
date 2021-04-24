
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.*;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.models.QnASearchResult;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.models.QnASearchResultList;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.models.QueryDTO;

import java.util.Scanner;

public class Tema5 {
    private static String CLAVE_RUNTIME = "bd655407-f0ca-4fb1-b649-b3c3f1b7ee0c";
    private static String ENDPOINT_RUNTIME = "https://tema5qnamaker.azurewebsites.net";
    private static String KB_ID = "0c149dbb-c0e4-4654-a1a2-36ecacdf66e4";

    public static void main(String[] args) {

        QnAMakerRuntimeClient clienteQnaMakerRuntime = QnAMakerRuntimeManager.authenticate(CLAVE_RUNTIME).withRuntimeEndpoint(ENDPOINT_RUNTIME);

        Scanner lectura =new Scanner(System.in);
        // Bucle de conversación
        while (true) {
            // Leemos la pregunta
            System.out.println("¿Qué quieres saber?");
            String pregunta = lectura.nextLine();

            // Invocamos a la API para obtener la respuesta
            QnASearchResultList respuestas = clienteQnaMakerRuntime.runtimes().generateAnswer(KB_ID, (new QueryDTO()).withQuestion(pregunta));

            // Mostramos la respuesta
            System.out.println("Respuestas:");
            for (QnASearchResult respuesta : respuestas.answers()) {
                System.out.println(respuesta.id());
            }
        }
    }

}
