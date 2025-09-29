package com.jalvaviel.addon.utils;

import meteordevelopment.meteorclient.gui.renderer.packer.TextureRegion;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;

public class RenderUtils {
    public static void renderGif(HudRenderer renderer, int frameIndex, int framesPerColumn, int width, int height, int canvasWidth, int canvasHeight, Identifier texture, int x, int y, float scale) {
        int row = frameIndex % framesPerColumn;
        int column = frameIndex / framesPerColumn;
        TextureRegion textureRegion = new TextureRegion(width,height);

        textureRegion.x1 = (float) (column * width) / canvasWidth;
        textureRegion.y1 = (float) (row * height) / canvasHeight;
        textureRegion.x2 = (float) ((column + 1) * width) / canvasWidth;
        textureRegion.y2 = (float) ((row + 1) * height) / canvasHeight;

        texture(renderer.drawContext.getMatrices(),texture,x,y,textureRegion,width,height,scale);
    }

    public static void texture(MatrixStack stack, Identifier texture, int x, int y, TextureRegion textureRegion, int width, int height, float scale) {
        bindTexture(stack,texture,scale);
        Renderer2D.TEXTURE.texQuad(x, y, width * scale, height * scale, textureRegion, new Color(255, 255, 255, 255));
        renderTexture(stack);
    }

    public static void texture(MatrixStack stack, Identifier texture, int x, int y, int width, int height, float scale) {
        bindTexture(stack,texture,scale);
        Renderer2D.TEXTURE.texQuad(x, y, width * scale, height * scale, new Color(255, 255, 255, 255));
        renderTexture(stack);
    }

    private static void bindTexture(MatrixStack stack, Identifier texture, float scale) {
        stack.push();
        stack.scale(scale,scale,1.0f);
        GL.bindTexture(texture);
        Renderer2D.TEXTURE.begin();
    }

    private static void renderTexture(MatrixStack stack) {
        Renderer2D.TEXTURE.render(stack);
        stack.pop();
    }

    public static int getCurrentAnimationFrame(List<Integer> delays) {
        int total = 0;
        long time = Util.getMeasuringTimeMs() % delays.stream().mapToInt(d -> d * 10).sum();
        for (int i = 0; i < delays.size(); i++) {
            total += delays.get(i) * 10;
            if (time < total) return i;
        }
        return 0;
    }
}
