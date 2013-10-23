package com.tooploox.akademiamobile.natychmiastogram.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

public class ImageFilter {

    static public class Config {
        // using public fields just to avoid setters/getters code here
        // Although for any real-life case, it should be structured a lot different.
        public int depth;
        public double red;
        public double green;
        public double blue;

        Config(int depth, double red, double green, double blue) {
            this.depth = depth;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    // constant sepia (you may play around with those values for different effects)
    static Config sepia = new Config(40, 1.5, 0.6, 0.12);

    // constant grayscale (better not to play too much with that; depth is not used in this case)
    static Config grayscale = new Config(1, 0.3, 0.59, 0.11);

    // placeholder values, to avoid allocation inside loops
    private static int A, R, G, B;
    private static int pixel;

    public static Bitmap roundBitmapCorners(Bitmap bitmap, int color, int cornerDips, int borderDips, Context context) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips,
                context.getResources().getDisplayMetrics());
        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips,
                context.getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        return output;
    }
    
    public static Bitmap sepia(Bitmap src) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);

                pixel = grayscalePixel(pixel);
                pixel = sepiaPixel(pixel);

                // set new pixel color to output image
                bmOut.setPixel(x, y, pixel);
            }
        }
        // return final image
        return bmOut;
    }

    public static Bitmap grayscale(Bitmap src) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);

                pixel = grayscalePixel(pixel);

                // set new pixel color to output image
                bmOut.setPixel(x, y, pixel);
            }
        }
        // return final image
        return bmOut;
    }

    private static int grayscalePixel(int colorPixel) {
        // get color on each channel
        A = Color.alpha(colorPixel);
        R = Color.red(colorPixel);
        G = Color.green(colorPixel);
        B = Color.blue(colorPixel);
        // apply grayscale sample
        B = G = R = (int) (grayscale.red * R + grayscale.green * G + grayscale.blue * B);

        return Color.argb(A, R, G, B);
    }

    private static int sepiaPixel(int grayscalePixel) {
        // get color on each channel
        A = Color.alpha(grayscalePixel);
        R = Color.red(grayscalePixel);
        G = Color.green(grayscalePixel);
        B = Color.blue(grayscalePixel);

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

        return Color.argb(A, R, G, B);
    }
}
