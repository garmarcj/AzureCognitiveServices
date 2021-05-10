import com.microsoft.azure.cognitiveservices.vision.contentmoderator.*;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tema6 {
    private static final String CLAVE_CONTENTMODERATOR = "3c18413e9ddd453192d48edfef5c7b1f";
    private static final String ENDPOINT_CONTENTMODERATOR = "https://tema6contentmoderator.cognitiveservices.azure.com/";
    private static final String IMAGEN_URL = "https://moderatorsampleimages.blob.core.windows.net/samples/sample1.jpg";

    private static final String CLAVE_REVISOR = "08370cfc1941477daf7498bddbca1568";
    private static final String ENDPOINT_REVISOR = "https://brazilsouth.api.cognitive.microsoft.com/";
    private static final String IDEQUIPO_REVISOR = "equipocontentmoderator";

    public static void main(String[] args) throws InterruptedException {

        // Inicializamos el cliente de ContentModerator con el endpoint y la clave
        ContentModeratorClient clienteContentModerator = ContentModeratorManager.authenticate(AzureRegionBaseUrl.fromString(ENDPOINT_CONTENTMODERATOR), CLAVE_CONTENTMODERATOR);

        //
        // Bloque de moderación de imagen (image-evaluate)
        //
        System.out.println("Moderación de la imagen...");
        // Preparamos la imagen a analizar
        BodyModelModel url = new BodyModelModel();
        url.withDataRepresentation("URL");
        url.withValue(IMAGEN_URL);
        Thread.sleep(1000);
        // Invocamos el método de la API para el análisis de la imagen
        Evaluate resultadoEvaluacion = clienteContentModerator.imageModerations().evaluateUrlInput("application/json", url, new EvaluateUrlInputOptionalParameter().withCacheImage(true));
        String esContenidoAdulto = String.valueOf(resultadoEvaluacion.isImageAdultClassified());
        String esContenidoSugerente = String.valueOf(resultadoEvaluacion.isImageRacyClassified());
        String puntuacionContenidoAdulto = String.valueOf(resultadoEvaluacion.adultClassificationScore());
        String puntuacionContenidoSugerente= String.valueOf(resultadoEvaluacion.racyClassificationScore());

        // Procesamos el resultado
        System.out.println("\tEvaluación de contenido para adultos "
                + esContenidoAdulto
                + ", con una puntuación de "
                + puntuacionContenidoAdulto);
        System.out.println("\tEvaluación de contenido sugerente "
                + esContenidoSugerente
                + ", con una puntuación de "
                + puntuacionContenidoSugerente);

        //
        // Bloque de imagen en la lista (image-match)
        //
        System.out.println("\nComprobación de imagen en la lista...");
        // Creamos la lista de imágenes
        String listaImagenes = crearLista(clienteContentModerator);

        // Invocamos el método de la API para la comprobación de la imagen en la lista
        Thread.sleep(1000);
        MatchResponse resultadoCoincidencia = clienteContentModerator.imageModerations().matchUrlInput("application/json",url,new MatchUrlInputOptionalParameter());

        // Procesamos el resultado
        if (resultadoCoincidencia.isMatch()) {
            System.out.println("\tLa imagen está presente en la lista personalizada");
        } else System.out.println("\tLa imagen no está presente en la lista personalizada");

        //
        // Bloque de gestión de la revisión
        //
        System.out.println("\nRevisión de la imagen...");
        // Inicializamos el cliente de la revisión del ContentModerator (que es diferente al cliente del ContentModerator) con el endpoint y la clave
        ContentModeratorClient clienteRevisionContentModerator = ContentModeratorManager.authenticate(AzureRegionBaseUrl.fromString(ENDPOINT_REVISOR), CLAVE_REVISOR);

        // Preparamos los metadatos para valorar una imagen
        List<CreateReviewBodyItemMetadataItem> metadatos = new ArrayList<>();
        metadatos.add(new CreateReviewBodyItemMetadataItem().withKey("esContenidoAdulto").withValue(esContenidoAdulto));
        metadatos.add(new CreateReviewBodyItemMetadataItem().withKey("esContenidoSugerente").withValue(esContenidoSugerente));
        metadatos.add(new CreateReviewBodyItemMetadataItem().withKey("puntuacionContenidoAdulto").withValue(puntuacionContenidoAdulto));
        metadatos.add(new CreateReviewBodyItemMetadataItem().withKey("puntuacionContenidoSugerente").withValue(puntuacionContenidoSugerente));
        metadatos.add(new CreateReviewBodyItemMetadataItem().withKey("hayCoincidencia").withValue(String.valueOf(resultadoCoincidencia.isMatch())));

        // Invocamos el método de la API para crear la revisión
        CreateReviewBodyItem revision = new CreateReviewBodyItem().withType("image").withContent(IMAGEN_URL).withMetadata(metadatos);
        List<CreateReviewBodyItem> listaRevisiones = new ArrayList<>();
        listaRevisiones.add(revision);
        List<String> listaIdRevision = clienteRevisionContentModerator.reviews().createReviews().withTeamName(IDEQUIPO_REVISOR).withUrlContentType("application/json").withCreateReviewBody(listaRevisiones).execute();

        // Esperamos a que se valide por el revisor humano en el portal
        System.out.println("\tLa revisión ya está preparada.");
        System.out.println("\tAhora es necesaria la validación manual de la revisión en el portal de Content Moderator.");
        System.out.println("\tUna vez se haya realizado, pulsa cualquier tecla para continuar...");
        Scanner teclado = new Scanner(System.in);
        teclado.nextLine();

        // Invocamos el método de la API para consultar la revisión
        Review resultado = clienteRevisionContentModerator.reviews().getReview(IDEQUIPO_REVISOR,listaIdRevision.get(0));
        if (resultado.status().equals("Complete")){
            for (KeyValuePair etiqueta : resultado.reviewerResultTags()) {
                System.out.println("\tEtiquetas: ");
                System.out.println("\t\t|-> " + etiqueta.key() + " con la descripción " + etiqueta.value());
            }
        }

        // Eliminamos la lista de imágenes
        eliminarLista(clienteContentModerator, listaImagenes);
    }

    //
    // Método estático crearLista
    // Crea una lista
    //
    public static String crearLista(ContentModeratorClient cmc) throws InterruptedException {
        // Creamos la lista
        Thread.sleep(1000);
        ImageList listaImagenes = cmc.listManagementImageLists().create("application/json",new BodyModel().withName("imagenes_prohibidas"));
        String listaId = listaImagenes.id().toString();

        // Añadimos las imágenes
        String imagenURL = "https://moderatorsampleimages.blob.core.windows.net/samples/sample1.jpg";
        BodyModelModel url = new BodyModelModel();
        url.withDataRepresentation("URL");
        url.withValue(imagenURL);
        Thread.sleep(1000);
        cmc.listManagementImages().addImageUrlInput().withListId(listaId).withContentType("application/json").withImageUrl(url).execute();

        // Refrescamos el índice de la lista
        Thread.sleep(1000);
        cmc.listManagementImageLists().refreshIndexMethod(listaId);
        Thread.sleep(100000);

        return listaId;
    }
    //
    // FIN - Método estático crearLista
    //

    //
    // Método estático eliminarLista
    // Elimina una lista
    //
    public static void eliminarLista(ContentModeratorClient cmc, String idLista) throws InterruptedException {
        // Eliminamos la lista
        Thread.sleep(1000);
        cmc.listManagementImageLists().delete(idLista);
    }
    //
    // FIN - Método estático eliminarLista
    //

}