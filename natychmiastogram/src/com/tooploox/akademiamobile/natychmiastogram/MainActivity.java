package com.tooploox.akademiamobile.natychmiastogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tooploox.akademiamobile.natychmiastogram.util.BitmapUtil;
import com.tooploox.akademiamobile.natychmiastogram.util.ImageFilter;

public class MainActivity extends Activity {

    protected static final int RC_IMAGE_CAPTURE = 0;
    protected static final int RC_GET_PICTURE = 1;

    protected ImageView ivPicture;

    String currentPicturePath = null;

    Bitmap currentlyDisplayedBitmap = null;

    protected void afterSetContentView() {
        ivPicture = (ImageView) findViewById(R.id.iv_picture);

        ivPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Tapnąłeś mnie!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Set the file, where you want the file to be saved.
                // Required for cross-device compatibility
                File imageFile = createImageFile();
                Uri imageUri = Uri.fromFile(imageFile);

                currentPicturePath = imageFile.getAbsolutePath();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, RC_IMAGE_CAPTURE);
            }
        });

        ivPicture.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(MainActivity.this, "Tapnąłeś mnie, ale tym razem długo. Jesteś boski!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, RC_GET_PICTURE);

                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_IMAGE_CAPTURE) {
                LoadBitmapTask task = new LoadBitmapTask();
                Uri uri = Uri.fromFile(new File(currentPicturePath));

                task.execute(uri);
            } else if (requestCode == RC_GET_PICTURE) {
                LoadBitmapTask task = new LoadBitmapTask();
                Uri uri = data.getData();

                task.execute(uri);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Nie to nie! Foch!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Auć, wystąpił problem z zewnętrzną aplikacją :/", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        // clean the mess we created on external memory :)
        File storageDir = getStorage();
        if (storageDir != null && storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            storageDir.delete();
        }
        // If the bitmap was already used and not recycled, recycle it to release memory
        if (currentlyDisplayedBitmap != null && !currentlyDisplayedBitmap.isRecycled()) {
            currentlyDisplayedBitmap.recycle();
            currentlyDisplayedBitmap = null;
        }
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        afterSetContentView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; set options in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // handle options item selected
        switch (item.getItemId()) {
            case R.id.action_sepia:
                applySepia();
                return true;
            case R.id.action_grayscale:
                applyGrayscale();
                return true;
            case R.id.action_save:
                saveBitmap();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void applySepia() {
        if (currentlyDisplayedBitmap != null) {
            ApplyFilterTask task = new ApplyFilterTask();
            task.execute(new BitmapFilter(currentlyDisplayedBitmap, BitmapFilter.Type.SEPIA));
        } else {
            Toast.makeText(this, "No ale nie ma jeszcze żadnego zdjęcia...", Toast.LENGTH_LONG).show();
        }
    }

    private void applyGrayscale() {
        if (currentlyDisplayedBitmap != null) {
            ApplyFilterTask task = new ApplyFilterTask();
            task.execute(new BitmapFilter(currentlyDisplayedBitmap, BitmapFilter.Type.GRAYSCALE));
        } else {
            Toast.makeText(this, "No ale nie ma jeszcze żadnego zdjęcia...", Toast.LENGTH_LONG).show();
        }
    }

    private void saveBitmap() {
        File picturesDir = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            + File.separator
            + "natychmiastogram"
        );

        if (!picturesDir.exists()) {
            if (!picturesDir.mkdirs()) {
                // TODO: error-handling
            }
        }

        String filename = "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File outFile = new File(picturesDir + File.separator + filename);

        if (currentlyDisplayedBitmap != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFile);
                currentlyDisplayedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                out.flush();
            } catch (IOException e) {
                Toast.makeText(this, "Coś nie poszło jak trzeba...", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    out.close();
                    Toast.makeText(this, String.format("Zdjęcie zapisane: %s", outFile.getAbsoluteFile()), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: add error-handling
                }
            }
        }
    }

    private File getStorage() {
        return new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/temp/");
    }

    private File createImageFile() {
        // Create a temporary image file
        File storageDir = getStorage();

        try {
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            return File.createTempFile("img", ".jpg", storageDir);
        } catch (Exception e) {
            Toast.makeText(this, "Hmm, na pewno możesz zapisywać na kartę SD?", Toast.LENGTH_LONG).show();
            return null;
        }
    }


    private static class BitmapFilter {
        public enum Type {
            SEPIA, GRAYSCALE;
        }

        public Bitmap src;
        public Type filterType;

        public BitmapFilter(Bitmap src, Type filterType) {
            this.src = src;
            this.filterType = filterType;
        }
    }

    private class ApplyFilterTask extends AsyncTask<BitmapFilter, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(BitmapFilter... params) {
            if (params != null && params.length > 0) {
                BitmapFilter requestedFilter = params[0];
                if (requestedFilter != null) {
                    switch (requestedFilter.filterType) {
                        case GRAYSCALE:
                            return ImageFilter.grayscale(requestedFilter.src);
                        case SEPIA:
                            return ImageFilter.sepia(requestedFilter.src);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            ivPicture.setImageBitmap(result);
            currentlyDisplayedBitmap.recycle();
            currentlyDisplayedBitmap = result;
        }
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];

            if (uri == null) {
                return null;
            }

            InputStream inStream = null;

            // NOTE: First check the size, then scale and load the image.
            //       See: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html

            try {
                ContentResolver cr = getContentResolver();
                inStream = cr.openInputStream(uri);

                // NOTE: This will cause a crash on large pictures (Out of Memory)
                //bitmap = BitmapFactory.decodeStream(inStream);

                // If the bitmap was already used, recycle it to release memory
                if (currentlyDisplayedBitmap != null) {
                    currentlyDisplayedBitmap.recycle();
                }

                // First check the size of the image
                BitmapFactory.Options checkOptions = new BitmapFactory.Options();
                checkOptions.inJustDecodeBounds = true;

                // Options now contain the size of the image
                BitmapFactory.decodeStream(inStream, null, checkOptions);

                inStream.close();

                // Calculate optimal sample size for the given dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = BitmapUtil.calculateInSampleSize(checkOptions, 640, 480);

                // Decode and scale the image
                inStream = cr.openInputStream(uri);

                currentlyDisplayedBitmap = BitmapFactory.decodeStream(inStream, null, options);

                inStream.close();
            } catch (FileNotFoundException e) {
                // TODO Meaningful errors messages
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Same as above
                e.printStackTrace();
            }

            return currentlyDisplayedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                ivPicture.setImageBitmap(result);
            }
        }
    }
}
