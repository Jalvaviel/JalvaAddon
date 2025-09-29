package com.jalvaviel.addon.hud;

import com.jalvaviel.addon.utils.ColorUtils;
import com.jalvaviel.addon.utils.RenderUtils;
import meteordevelopment.meteorclient.gui.renderer.packer.TextureRegion;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinWorkerThread;

import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NodeList;

import static com.jalvaviel.addon.Addon.*;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.opengl.GL11C.*;

public class ImageHud extends HudElement {
    public static final HudElementInfo<ImageHud> INFO = new HudElementInfo<>(HUD_GROUP, "image",
        "Cures your ADHD.", ImageHud::new);
    private static final int MAX_TEX_SIZE = glGetInteger(GL_MAX_TEXTURE_SIZE);
    private static final Identifier DEFAULT_TEXTURE = Identifier.of(MOD_ID,"textures/gui/default_image.png");
    private static final Identifier LOADING_TEXTURE = Identifier.of(MOD_ID,"textures/gui/loading_image.png");
    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    private boolean loading = false;
    private Identifier texture;
    private final List<Integer> delays = new ArrayList<>();
    private int width = 128;
    private int height = 128;
    private int canvasWidth, canvasHeight;
    private int framesPerColumn;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public ImageHud() {
        super(INFO);
        setSize(128,128);
    }

    private final Setting<String> path = sgGeneral.add(new StringSetting.Builder()
        .name("Path")
        .description("The full path / link of the image")
        .wide()
        .onChanged(this::composeImage)
        .build()
    );

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .defaultValue(1)
        .min(0.1)
        .sliderRange(0.5, 2)
        .max(10)
        .build()
    );

    @Override
    public void render(HudRenderer renderer) {
        //setSize(width * scale.get(),height * scale.get());
        if (texture == null && !loading) {
            RenderUtils.texture(renderer.drawContext.getMatrices(),DEFAULT_TEXTURE,getX(),getY(),128,128,scale.get().floatValue());
        } else if (loading) {
            RenderUtils.texture(renderer.drawContext.getMatrices(),LOADING_TEXTURE,getX(),getY(),128,128,scale.get().floatValue());
        } else if (delays.isEmpty()) {
            RenderUtils.texture(renderer.drawContext.getMatrices(),texture,getX(),getY(),width,height,scale.get().floatValue());
        } else {
            RenderUtils.renderGif(renderer, RenderUtils.getCurrentAnimationFrame(delays), framesPerColumn, width, height,
                canvasWidth, canvasHeight, texture, x, y, scale.get().floatValue());
        }
    }

    private void update(int width, int height) {
        mc.getTextureManager().destroyTexture(texture);
        texture = null;
        this.width = width;
        this.height = height;
        setSize(width*scale.get(),height*scale.get());
    }

    private void composeImage(String path) {
        update(128, 128);
        delays.clear();
        String parsed = path.replace("\"", "").replace("\\", "/");
        String name = path.substring(parsed.lastIndexOf("/") + 1);
        InputStream imageFile;
        try {
            imageFile = path.startsWith("http") ? new URL(path).openStream() : new FileInputStream(parsed);
            ImageInputStream stream = ImageIO.createImageInputStream(imageFile);
            loading = true;
            workerThread.submit(() -> {
                try {
                    ImageReader reader = ImageIO.getImageReaders(stream).next();
                    reader.setInput(stream);
                    String format = reader.getFormatName();
                    BufferedImage canvas;
                    int imageWidth = reader.getWidth(0);
                    int imageHeight = reader.getHeight(0);
                    if (format.equals("gif")) {
                        canvas = composeGif(reader, imageWidth, imageHeight);
                    } else {
                        canvas = reader.read(0);
                    }
                    update(imageWidth, imageHeight);
                    NativeImage tex =  ColorUtils.bufferedToNative(canvas);
                    registerTexture(name,tex);
                    loading = false;
                } catch (Exception ignored) {}
                return null;
            });

        } catch (Exception ignored) {}
    }

    private BufferedImage composeGif(ImageReader reader, int width, int height) throws IOException {
        int totalFrames = reader.getNumImages(true);
        framesPerColumn = MAX_TEX_SIZE / height;
        int columns = (int) Math.ceil(totalFrames / (double) framesPerColumn);
        canvasWidth = width*columns;
        canvasHeight = Math.min(height*framesPerColumn,MAX_TEX_SIZE);
        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvasGraphics = canvas.createGraphics();
        int lastUndisposed = 0;
        for (int i = 0; i < totalFrames; i++) {
            IIOMetadata metadata = reader.getImageMetadata(i);
            FrameMetadata frameMetadata = fromGIF(metadata);
            delays.add(frameMetadata.delay);
            Offset offset = getFrameOffset(i, framesPerColumn, width, height);
            switch(frameMetadata.disposal) {
                case "doNotDispose" -> { // Set the background to the current undisposed frame and draw the next frame over.
                    Offset prevOffset = (i==0) ? new Offset(0,0) : getFrameOffset(lastUndisposed, framesPerColumn, width, height);
                    composeWithDisposal(reader.read(i),canvas,canvasGraphics,frameMetadata,offset,prevOffset,width,height);
                    lastUndisposed = i;
                }
                case "restoreToPrevious" -> { // Set the background to the previous undisposed frame and draw the next frame over.
                    Offset prevOffset = getFrameOffset(lastUndisposed, framesPerColumn,width, height);
                    composeWithDisposal(reader.read(i),canvas,canvasGraphics,frameMetadata,offset,prevOffset,width,height);
                }
                case "restoreToBackground" -> { // Set the background color and draw the next frame over.
                    canvasGraphics.setColor(frameMetadata.backgroundColor);
                    canvasGraphics.fillRect(offset.x, offset.y, width, height);
                    canvasGraphics.drawImage(reader.read(i),offset.x+frameMetadata.offset.x, offset.y+frameMetadata.offset.y, null);
                }
                default -> canvasGraphics.drawImage(reader.read(i),offset.x, offset.y, null); // Just draw the next frame as fallback.
            }
        }
        canvasGraphics.dispose();
        return canvas;
    }

    private void composeWithDisposal(BufferedImage next, BufferedImage canvas, Graphics2D canvasGraphics,
                                     FrameMetadata frameMetadata, Offset offset, Offset prevOffset, int width, int height) {
        canvasGraphics.drawImage(canvas,
            offset.x, offset.y,
            offset.x+width, offset.y+height,
            prevOffset.x,prevOffset.y,
            prevOffset.x+width,prevOffset.y+height
            ,null);
        canvasGraphics.drawImage(next,
            offset.x + frameMetadata.offset.y, offset.y + frameMetadata.offset.x, // THIS WORKS
            null);
        //canvasGraphics.drawString(String.valueOf(i), offset.x + frameMetadata.offset.x, offset.y + frameMetadata.offset.y);
    }

    private Offset getFrameOffset(int i, int framesPerColumn, int width, int height) {
        int col = i / framesPerColumn;
        int row = i % framesPerColumn;
        int x = col * width;
        int y = row * height;
        return new Offset(x,y);
    }

    private static FrameMetadata fromGIF(IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
        IIOMetadataNode desc = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
        int delay = Integer.parseInt(gce.getAttribute("delayTime"));
        String disposal = gce.getAttribute("disposalMethod");
        int leftPos = Integer.parseInt(desc.getAttribute("imageTopPosition"));
        int topPos = Integer.parseInt(desc.getAttribute("imageLeftPosition"));
        if (disposal.equals("restoreToBackground")) return new FrameMetadata(delay, disposal, new Offset(leftPos,topPos), getBgColor(metadata));
        return new FrameMetadata(delay, disposal, new Offset(leftPos,topPos), new Color(0,0,0));
    }

    private static Color getBgColor(IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
        IIOMetadataNode lsd = (IIOMetadataNode) root.getElementsByTagName("LogicalScreenDescriptor").item(0); // LOL
        IIOMetadataNode gct =  (IIOMetadataNode) root.getElementsByTagName("GlobalColorTable").item(0);
        int bgcIndex = Integer.parseInt(lsd.getAttribute("backgroundColorIndex"));
        return getBgColorInternal(gct, bgcIndex);
    }

    private static @NotNull Color getBgColorInternal(IIOMetadataNode gct, int bgcIndex) {
        Color bgColor = new Color(0,0,0);
        if (gct != null) {
            NodeList colorEntries = gct.getElementsByTagName("ColorTableEntry");
            for (int i = 0; i < colorEntries.getLength(); i++) {
                IIOMetadataNode colorEntry = (IIOMetadataNode) colorEntries.item(i);
                int colorIndex = Integer.parseInt(colorEntry.getAttribute("index"));
                if (colorIndex == bgcIndex) {
                    bgColor = new Color(Integer.parseInt(colorEntry.getAttribute("red")),
                        Integer.parseInt(colorEntry.getAttribute("green")),
                        Integer.parseInt(colorEntry.getAttribute("blue")));
                }
            }
        }
        return bgColor;
    }

    private void registerTexture(String name, NativeImage tex) {
        name = name.toLowerCase().replaceAll("[^a-z0-9/._-]","_");
        if (tex == null) {
            LOG.warn("Could register the image " + name + " because the image is null.");
            return;
        }
        texture = Identifier.of(MOD_ID,name);
        NativeImageBackedTexture backedTexture = new NativeImageBackedTexture(tex);
        mc.getTextureManager().registerTexture(texture, backedTexture);
    }

    private record FrameMetadata(int delay, String disposal, Offset offset, Color backgroundColor) {}
    private record Offset(int x, int y) {}
}
