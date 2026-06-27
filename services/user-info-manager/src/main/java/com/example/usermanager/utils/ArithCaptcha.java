package com.example.usermanager.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 简单四则运算验证码生成器（仅加减法，纯JDK，无外部依赖）
 */
public class ArithCaptcha {

    private static final Random RAND = new Random();
    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;

    private final String code;    // 计算结果
    private final String base64;  // base64 图片

    public ArithCaptcha(int num1, String op, int num2, int result) {
        this.code = String.valueOf(result);
        String expr = num1 + " " + op + " " + num2 + " = ?";

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 5; i++) {
            int x1 = RAND.nextInt(WIDTH);
            int y1 = RAND.nextInt(HEIGHT);
            int x2 = RAND.nextInt(WIDTH);
            int y2 = RAND.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(RAND.nextInt(200), RAND.nextInt(200), RAND.nextInt(200)));
            g.fillOval(RAND.nextInt(WIDTH), RAND.nextInt(HEIGHT), 2, 2);
        }

        // 绘制表达式文字
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(expr);
        int x = (WIDTH - textWidth) / 2;
        int y = (HEIGHT - fm.getHeight()) / 2 + fm.getAscent();

        // 每个字符不同颜色
        char[] chars = expr.toCharArray();
        int charX = x;
        for (char c : chars) {
            g.setColor(new Color(RAND.nextInt(100), RAND.nextInt(120), RAND.nextInt(180)));
            String s = String.valueOf(c);
            g.drawString(s, charX, y + RAND.nextInt(4) - 2);
            charX += fm.stringWidth(s);
        }

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            this.base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    public static ArithCaptcha create() {
        int num1 = RAND.nextInt(9) + 1;  // 1-9
        int num2 = RAND.nextInt(9) + 1;  // 1-9
        // 确保减法结果不为负
        if (num1 < num2) { int t = num1; num1 = num2; num2 = t; }
        String op = RAND.nextBoolean() ? "+" : "-";
        int result = "+".equals(op) ? num1 + num2 : num1 - num2;
        return new ArithCaptcha(num1, op, num2, result);
    }

    public String getCode() { return code; }

    public String getBase64() { return base64; }
}
