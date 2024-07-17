package kz.awsstudio.pixaiapi.PixAIService;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kz.awsstudio.pixaiapi.download.ImageDownloader;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PixAIService {

    private String apiKey;
    private JSONObject photoConfig;
    private OkHttpClient client;

    /**
     * Конструктор для инициализации сервиса PixAI.
     * @param apiKey API ключ для доступа к PixAI API.
     * @param photoConfig конфигурационные параметры для генерации фотографий.
     * @param imageDownloader экземпляр класса ImageDownloader для загрузки изображений.
     */
    public PixAIService(String apiKey, JSONObject photoConfig, ImageDownloader imageDownloader) {
        this.apiKey = apiKey;
        this.photoConfig = photoConfig;
        this.client = new OkHttpClient();
    }

    /**
     * Создает задачу на генерацию изображения.
     * @param prompt текстовый запрос для генерации изображения.
     * @return идентификатор задачи.
     * @throws IOException если запрос не был успешным.
     */
    public String createGenerationTask(String prompt) throws IOException {
        String graphqlQuery = "mutation createGenerationTask($parameters: JSONObject!) { createGenerationTask(parameters: $parameters) { id } }";

        JSONObject variables = new JSONObject();
        JSONObject parameters = new JSONObject(photoConfig.toString());
        parameters.put("prompts", prompt);
        variables.put("parameters", parameters);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", graphqlQuery);
        jsonBody.put("variables", variables);

        @SuppressWarnings("deprecation")
		RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url("https://api.pixai.art/graphql")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response + " with message: " + response.body().string());
        }

        String responseBodyString = response.body().string();
        JSONObject responseBody = new JSONObject(responseBodyString);

        return responseBody.getJSONObject("data").getJSONObject("createGenerationTask").getString("id");
    }

    /**
     * Проверяет статус задачи.
     * @param taskId идентификатор задачи.
     * @return статус задачи.
     * @throws IOException если запрос не был успешным.
     * @throws InterruptedException если ожидание было прервано.
     */
    public String pollTaskStatus(String taskId) throws IOException, InterruptedException {
        String graphqlQuery = "query getTaskById($id: ID!) { task(id: $id) { id status } }";

        JSONObject variables = new JSONObject();
        variables.put("id", taskId);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", graphqlQuery);
        jsonBody.put("variables", variables);

        while (true) {
            @SuppressWarnings("deprecation")
			Request request = new Request.Builder()
                    .url("https://api.pixai.art/graphql")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody.toString()))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " with message: " + response.body().string());
            }

            JSONObject responseBody = new JSONObject(response.body().string());
            String status = responseBody.getJSONObject("data").getJSONObject("task").getString("status");

            if ("completed".equals(status) || "failed".equals(status) || "cancelled".equals(status)) {
                return status;
            }

            Thread.sleep(5000);
        }
    }

    /**
     * Получает идентификатор медиафайла.
     * @param taskId идентификатор задачи.
     * @return идентификатор медиафайла.
     * @throws IOException если запрос не был успешным.
     */
    public String getMediaId(String taskId) throws IOException {
        String graphqlQuery = "query getTaskById($id: ID!) { task(id: $id) { outputs } }";

        JSONObject variables = new JSONObject();
        variables.put("id", taskId);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", graphqlQuery);
        jsonBody.put("variables", variables);

        @SuppressWarnings("deprecation")
		RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url("https://api.pixai.art/graphql")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response + " with message: " + response.body().string());
        }

        String responseBodyString = response.body().string();
        JSONObject responseBody = new JSONObject(responseBodyString);

        JSONObject outputs = responseBody.getJSONObject("data").getJSONObject("task").getJSONObject("outputs");
        String mediaId = outputs.optString("mediaId", null);
        if (mediaId == null) {
            throw new JSONException("JSONObject['mediaId'] not found.");
        }
        return mediaId;
    }

    /**
     * Загружает сгенерированное изображение.
     * @param mediaId идентификатор медиафайла.
     * @throws IOException если запрос не был успешным.
     */
    public void downloadGeneratedImage(String mediaId) throws IOException {
        String graphqlQuery = "query getMediaById($id: String!) { media(id: $id) { urls { variant url } } }";

        JSONObject variables = new JSONObject();
        variables.put("id", mediaId);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", graphqlQuery);
        jsonBody.put("variables", variables);

        @SuppressWarnings("deprecation")
		RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url("https://api.pixai.art/graphql")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response + " with message: " + response.body().string());
        }

        String responseBodyString = response.body().string();
        JSONObject responseBody = new JSONObject(responseBodyString);

        JSONArray urls = responseBody.getJSONObject("data").getJSONObject("media").getJSONArray("urls");
        String downloadUrl = null;

        // Attempt to find a suitable download URL
        for (int i = 0; i < urls.length(); i++) {
            JSONObject urlObj = urls.getJSONObject(i);
            if (urlObj.has("url")) {
                downloadUrl = urlObj.getString("url");
                break;
            }
        }


        ImageDownloader.downloadFile(downloadUrl);
    }
}
