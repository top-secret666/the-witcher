package main.java.com.witcher.ui.graphics;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Декодирование GIF в массив кадров для анимаций. */
public final class GifFrames {

    public final BufferedImage[] frames;
    public final int[] delaysMs;

    private GifFrames(BufferedImage[] frames, int[] delaysMs) {
        this.frames = frames;
        this.delaysMs = delaysMs;
    }

    public static GifFrames loadFirst(String... paths) {
        for (String path : paths) {
            GifFrames data = load(path);
            if (data != null) return data;
        }
        return null;
    }

    public static GifFrames load(String resourcePath) {
        try {
            InputStream is = GifFrames.class.getResourceAsStream(resourcePath);
            if (is == null) {
                String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                File file = Paths.get(System.getProperty("user.dir"), "src", "main", "resources")
                    .resolve(relative).toFile();
                if (!file.exists()) return null;
                is = new java.io.FileInputStream(file);
            }

            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) return null;

            ImageReader reader = readers.next();
            reader.setInput(iis);
            int count = reader.getNumImages(true);
            if (count == 0) return null;

            List<BufferedImage> frameList = new ArrayList<>();
            List<Integer> delayList = new ArrayList<>();

            int canvasW = reader.getWidth(0);
            int canvasH = reader.getHeight(0);
            BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < count; i++) {
                BufferedImage rawFrame = reader.read(i);
                int delayMs = 80;
                try {
                    javax.imageio.metadata.IIOMetadata meta = reader.getImageMetadata(i);
                    org.w3c.dom.Node tree = meta.getAsTree(meta.getNativeMetadataFormatName());
                    org.w3c.dom.NodeList children = tree.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        org.w3c.dom.Node child = children.item(j);
                        if ("GraphicControlExtension".equals(child.getNodeName())) {
                            org.w3c.dom.Node delayAttr = child.getAttributes().getNamedItem("delayTime");
                            if (delayAttr != null) {
                                delayMs = Integer.parseInt(delayAttr.getNodeValue()) * 10;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }

                Graphics2D cg = canvas.createGraphics();
                cg.drawImage(rawFrame, 0, 0, null);
                cg.dispose();

                BufferedImage snapshot = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D sg = snapshot.createGraphics();
                sg.drawImage(canvas, 0, 0, null);
                sg.dispose();

                frameList.add(snapshot);
                delayList.add(Math.max(20, delayMs));
            }
            reader.dispose();

            return new GifFrames(
                frameList.toArray(new BufferedImage[0]),
                delayList.stream().mapToInt(Integer::intValue).toArray()
            );
        } catch (Exception e) {
            return null;
        }
    }
}
