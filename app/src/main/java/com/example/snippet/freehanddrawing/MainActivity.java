
// custom view

package com.example.snippet.freehanddrawing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int READ_REQUEST_CODE  = 1;
    private final int WRITE_REQUEST_CODE = 2;

    private final int FLOAT_SIZE   = Float.SIZE / 8;     // float = 4 bytes
    private final int INTEGER_SIZE = Integer.SIZE / 8;   // integer = 4 bytes

    private final int BUFFER_SIZE = 2 * FLOAT_SIZE;      // 8 bytes

    private CharSequence name;

    private CustomView view;
    private Uri uri;
    private SaveFileTask savefiletask;
    private OpenFileTask openfiletask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uri = null;

        // set the display to custom view
        view = new CustomView(getApplicationContext());
        setContentView(view);

        name = getTitle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.clearArrayList();
    }

    // create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handle menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_open:   onMenuOpen();   break;
            case R.id.menu_save:   onMenuSave();   break;
            case R.id.menu_saveas: onMenuSaveAs(); break;
            case R.id.menu_clear:  onMenuClear();  break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    // enable/disable menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_save).setEnabled(!view.lines.isEmpty());
        menu.findItem(R.id.menu_saveas).setEnabled(!view.lines.isEmpty());
        menu.findItem(R.id.menu_clear).setEnabled(!view.lines.isEmpty());

        return super.onPrepareOptionsMenu(menu);
    }

    // handle the result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String str;

        // handle read resquest code
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data != null) {

                uri = data.getData();
                str = uri.toString();
                setTitle(name + " - " + str);

                openfiletask = new OpenFileTask(getApplicationContext());
                openfiletask.lines = view.lines;
                openfiletask.execute(uri);
            }
        }

        // handle write resquest code
        if (requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data != null) {

                uri = data.getData();
                str = uri.toString();
                setTitle(name + " - " + str);

                savefiletask = new SaveFileTask(getApplicationContext());
                savefiletask.lines = view.lines;
                savefiletask.execute(uri);
            }
        }
    }

    // open drawing
    private void onMenuOpen(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    // save drawing
    private void onMenuSave(){

        if(uri == null) {
            onMenuSaveAs();
        }else{
            savefiletask = new SaveFileTask(getApplicationContext());
            savefiletask.lines = view.lines;
            savefiletask.execute(uri);
        }
    }

    // save as drawing
    private void onMenuSaveAs(){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");

        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    // clear drawing on screen
    private void onMenuClear(){
        view.clearScreen();
    }

    // save file task
    private class SaveFileTask extends AsyncTask<Uri, Integer, String> {

        private Context context;

        public ArrayList<ArrayList<PointF>> lines;

        // constructor
        public SaveFileTask(Context context) {
            this.context = context;
            lines = null;
        }

        // before saving drawing
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // after saving drawing
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(context, s , Toast.LENGTH_SHORT).show();
        }

        // update display
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        // do the saving
        @Override
        protected String doInBackground(Uri... uris) {

            OutputStream os;
            ByteBuffer bb;
            byte[] b;
            String str;
            int i, j, m, n;
            ArrayList<PointF> points;
            PointF p;

            try {
                // open file for writing
                os = context.getContentResolver().openOutputStream(uris[0]);

                bb = ByteBuffer.allocate(BUFFER_SIZE);

                // line count
                n = lines.size();

                // write line count
                bb.putInt(n);
                b = bb.array();
                os.write(b, 0, INTEGER_SIZE);

                // enumerate lines
                for (i = 0; i < n; i++) {

                    // point count
                    points = lines.get(i);
                    m = points.size();

                    // write point count
                    bb.rewind();
                    bb.putInt(m);
                    b = bb.array();
                    os.write(b, 0, INTEGER_SIZE);

                    // enumerate points
                    for (j = 0; j < m; j++) {

                        p = points.get(j);

                        // write point values
                        bb.rewind();
                        bb.putFloat(p.x);
                        bb.putFloat(p.y);
                        b = bb.array();
                        os.write(b);
                    }
                }

                // close file
                os.close();

                str = "Na save na.";

            } catch (IOException e) {
                str = e.toString();
            }

            return str;
        }
    }

    // open file task
    private class OpenFileTask extends AsyncTask<Uri, Integer, String>{

        private Context context;

        public ArrayList<ArrayList<PointF>> lines;

        // constructor
        public OpenFileTask(Context context) {
            super();

            this.context = context;
            lines = null;
        }

        // before opening drawing
        @Override
        protected void onPreExecute() {
            view.clearArrayList();
        }

        // after opening drawing
        @Override
        protected void onPostExecute(String s) {

            view.refresh();

            if(s != null) Toast.makeText(context, s , Toast.LENGTH_SHORT).show();
        }

        // update display
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        // do the opening
        @Override
        protected String doInBackground(Uri... uris) {

            InputStream is;
            ByteBuffer bb;
            byte[] b = new byte[BUFFER_SIZE];
            String str;
            int i, j, m, n;
            float x, y;

            ArrayList<PointF> points;

            try{
                // open file for reading
                is = context.getContentResolver().openInputStream(uri);

                // read line count
                is.read(b, 0, INTEGER_SIZE);
                bb = ByteBuffer.wrap(b);
                n = bb.getInt();

                // read line one at a time
                for (i = 0; i < n; i++){

                    // read point count
                    is.read(b, 0, INTEGER_SIZE);
                    bb = ByteBuffer.wrap(b);
                    m = bb.getInt();

                    // create array of points
                    points = new ArrayList<PointF>();

                    // read point one at a time
                    for (j = 0; j < m; j++){

                        // read point values
                        is.read(b, 0, BUFFER_SIZE);
                        bb = ByteBuffer.wrap(b);
                        x = bb.getFloat();
                        y = bb.getFloat();

                        // add points to an array of points
                        points.add(new PointF(x, y));
                    }

                    // add array of points to array of lines
                    lines.add(points);
                }

                // close file
                is.close();

                str = null;

            } catch (FileNotFoundException e) {
                str = e.toString();
            } catch (IOException e) {
                str = e.toString();
            }

            return str;
        }
    }
}
