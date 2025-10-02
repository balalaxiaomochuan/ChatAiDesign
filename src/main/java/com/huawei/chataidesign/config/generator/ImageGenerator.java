package com.huawei.chataidesign.config.generator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageGenerator {

    /**
     * 生成包含指定字母的小分辨率图像，并返回Base64编码
     *
     * @param letter 要显示的字母字符
     * @return 图像的Base64编码字符串
     * @throws IOException 当图像处理发生错误时
     */
    public static String generateLetterImageBase64(char letter) throws IOException {
        // 设置图像尺寸
        int width = 64;
        int height = 64;

        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 生成随机背景色
        Color backgroundColor = new Color(
                (int)(Math.random() * 256),
                (int)(Math.random() * 256),
                (int)(Math.random() * 256)
        );

        // 填充背景
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        // 设置文字颜色（与背景色形成对比）
        Color textColor = getContrastColor(backgroundColor);
        g2d.setColor(textColor);

        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(font);

        // 计算文字位置使其居中
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(String.valueOf(letter))) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();

        // 绘制文字
        g2d.drawString(String.valueOf(letter), x, y);

        // 释放资源
        g2d.dispose();

        // 转换为Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 根据背景色生成对比色
     *
     * @param backgroundColor 背景色
     * @return 对比色
     */
    private static Color getContrastColor(Color backgroundColor) {
        // 计算亮度
        double brightness = (0.299 * backgroundColor.getRed() +
                0.587 * backgroundColor.getGreen() +
                0.114 * backgroundColor.getBlue()) / 255;

        // 如果背景较暗，使用白色；否则使用黑色
        if (brightness < 0.5) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }
}

