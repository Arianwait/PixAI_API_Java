package kz.awsstudio.pixaiapi;

import java.io.IOException;

import kz.awsstudio.pixaiapi.client.PixAIPlugin;

public class PixAIExample {

    public static void main(String[] args) {
        try {
            PixAIPlugin plugin = new PixAIPlugin("config.json");
            plugin.run("girl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
