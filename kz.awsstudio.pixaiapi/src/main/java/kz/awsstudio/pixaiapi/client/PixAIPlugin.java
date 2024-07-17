package kz.awsstudio.pixaiapi.client;

import java.io.IOException;

import kz.awsstudio.pixaiapi.PixAIService.PixAIService;
import kz.awsstudio.pixaiapi.config.PixAIConfig;
import kz.awsstudio.pixaiapi.download.ImageDownloader;

public class PixAIPlugin {

    private PixAIService pixAIService;

    /**
     * Конструктор для инициализации плагина с конфигурацией.
     * @param configFilePath путь к конфигурационному файлу.
     * @throws IOException если конфигурационный файл не найден или не удалось его прочитать.
     */
    public PixAIPlugin(String configFilePath) throws IOException {
        PixAIConfig config = new PixAIConfig(configFilePath);
        ImageDownloader imageDownloader = new ImageDownloader(config.getApiKey());
        this.pixAIService = new PixAIService(config.getApiKey(), config.getPhotoConfig(), imageDownloader);
    }

    /**
     * Метод для запуска плагина.
     */
    public void run(String prompt) {
        try {
            // Создание задачи на генерацию изображения
            String taskId = pixAIService.createGenerationTask(prompt);
            System.out.println("Task created: " + taskId);

            // Проверка статуса задачи
            String status = pixAIService.pollTaskStatus(taskId);
            System.out.println("Task status: " + status);

            // Загрузка сгенерированного изображения
            if ("completed".equals(status)) {
                String mediaId = pixAIService.getMediaId(taskId);
                System.out.println("Media ID: " + mediaId);
                pixAIService.downloadGeneratedImage(mediaId);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
