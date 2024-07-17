package kz.awsstudio.pixaiapi.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageDownloader {

    private static OkHttpClient client;
    private static String API_KEY;
    /**
     * Конструктор для инициализации загрузчика изображений.
     */
    public ImageDownloader(String API_KEY) {
        ImageDownloader.client = new OkHttpClient();
        ImageDownloader.API_KEY = API_KEY;
    }

    /**
     * Загружает файл по указанному URL.
     * @param downloadUrl URL для загрузки файла.
     * @throws IOException если запрос не был успешным.
     */
    public static void downloadFile(String downloadUrl) throws IOException {
        // Request for downloading the image
        Request request = new Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        // Send request and get response
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response + " with message: " + response.body().string());
        }

        // Read data from response and save to file
        byte[] imageBytes = response.body().bytes();

        // Create file path with current date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "picture_PixAI_" + timeStamp + ".png";
        String folderPath = "PixAI";
        Files.createDirectories(Paths.get(folderPath)); // Create folder if it doesn't exist

        try (FileOutputStream fos = new FileOutputStream(Paths.get(folderPath, fileName).toString())) {
            fos.write(imageBytes);
            System.out.println("Image downloaded: " + folderPath + "/" + fileName);
        }
    }
}