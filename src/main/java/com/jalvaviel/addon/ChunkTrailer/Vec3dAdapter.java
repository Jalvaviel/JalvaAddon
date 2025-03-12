package com.jalvaviel.addon.ChunkTrailer;


import com.google.gson.*;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Type;

public class Vec3dAdapter implements JsonSerializer<Vec3d>, JsonDeserializer<Vec3d> {
    @Override
    public JsonElement serialize(Vec3d src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("x", src.x);
        json.addProperty("y", src.y);
        json.addProperty("z", src.z);
        return json;
    }

    @Override
    public Vec3d deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new Vec3d(obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble());
    }
}
