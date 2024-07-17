package kz.awsstudio.pixaiapi.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;


public class PixAIConfig {

    private String apiKey;
    private JSONObject photoConfig;

    /**
     * Конструктор для загрузки конфигурационного файла.
     * @param configFilePath путь к конфигурационному файлу.
     * @throws IOException если файл не найден или не удалось его прочитать.
     */
    public PixAIConfig(String configFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
        JSONObject json = new JSONObject(content);
        this.apiKey = json.getString("apiKey");
        this.photoConfig = json.getJSONObject("photoConfig");
    }

    /**
     * Получить API ключ.
     * @return API ключ.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Получить конфигурацию для фотографий.
     * @return конфигурация для фотографий в формате JSON.
     */
    public JSONObject getPhotoConfig() {
        return photoConfig;
    }
}

