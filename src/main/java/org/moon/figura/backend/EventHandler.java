package org.moon.figura.backend;

import com.google.gson.JsonObject;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;

import java.util.UUID;
import java.util.function.BiConsumer;

public enum EventHandler {

    UPLOAD((owner, json) -> {
        if (FiguraMod.isLocal(owner))
            AvatarManager.localUploaded = true;
        else
            AvatarManager.reloadAvatar(owner);

        //re-sub
        NetworkManager.subscribe(owner);
    }),
    EQUIP((owner, json) -> {

    }),
    PING((owner, json) -> {

    }),
    DELETE((owner, json) -> {
        if (!FiguraMod.isLocal(owner) || AvatarManager.localUploaded)
            AvatarManager.clearAvatar(owner);
    });

    private final BiConsumer<UUID, JsonObject> consumer;

    EventHandler(BiConsumer<UUID, JsonObject> consumer) {
        this.consumer = consumer;
    }

    public static void readEvent(UUID owner, JsonObject event) {
        try {
            if (!event.has("type"))
                throw new Exception();
        } catch (Exception ignored) {
            FiguraMod.LOGGER.warn("Invalid backend event: " + NetworkManager.GSON.toJson(event));
            return;
        }

        String type = event.get("type").getAsString().toUpperCase();
        try {
            EventHandler handler = EventHandler.valueOf(type);
            handler.consumer.accept(owner, event);
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Invalid backend event type: " + type, e);
        }
    }
}
