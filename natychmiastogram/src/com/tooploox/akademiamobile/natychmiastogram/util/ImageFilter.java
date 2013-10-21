package com.tooploox.akademiamobile.natychmiastogram.util;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageFilter {

    static public class Config {
        int depth;
        double red;
        double green;
        double blue;

        Config(int depth, double red, double green, double blue) {
            this.depth = depth;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    // constant sepia
    static Config sepia = new Config(40, 1.5, 0.6, 0.12);
    // constant grayscale
    static Config grayscale = new Config(1, 0.3, 0.59, 0.11);

    public static Bitmap sepia(Bitmap src) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int) (grayscale.red * R + grayscale.green * G + grayscale.blue * B);

                // apply intensity level for sepid-toning on each channel
                R += (sepia.depth * sepia.red);
                if (R > 255) {
                    R = 255;
                }

                G += (sepia.depth * sepia.green);
                if (G > 255) {
                    G = 255;
                }

                B += (sepia.depth * sepia.blue);
                if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static Bitmap grayscale(Bitmap inBitmap) {
        return null;
    }
}
