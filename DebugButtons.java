import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class DebugButtons {
    public static void main(String[] args) throws Exception {
        BufferedImage src = ImageIO.read(new File("src/main/resources/assets/sprites/menu/menu_buttons_sheet.png"));
        int cw = src.getWidth() / 3;
        int ch = src.getHeight() / 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                BufferedImage frame = src.getSubimage(c * cw, r * ch, cw, ch);
                int minX = frame.getWidth(), minY = frame.getHeight(), maxX = -1, maxY = -1;
                for (int y = 0; y < frame.getHeight(); y++) {
                    for (int x = 0; x < frame.getWidth(); x++) {
                        int a = (frame.getRGB(x, y) >>> 24) & 0xFF;
                        if (a > 4) {
                            if (x < minX) minX = x;
                            if (y < minY) minY = y;
                            if (x > maxX) maxX = x;
                            if (y > maxY) maxY = y;
                        }
                    }
                }
                System.out.println("r=" + r + " c=" + c + " : minX=" + minX + " minY=" + minY + " w=" + (maxX - minX + 1) + " h=" + (maxY - minY + 1));
            }
        }
    }
}
