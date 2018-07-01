
// save, open free hand drawing

package com.example.snippet.freehanddrawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class CustomView extends View {

    private Paint paint;
    private ArrayList<PointF> points;

    public ArrayList<ArrayList<PointF>> lines;

    // constructor
    public CustomView(Context context) {
        super(context);

        paint = new Paint();
        lines = new ArrayList<ArrayList<PointF>>();
        points = new ArrayList<PointF>();

    }

    // render screen
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int i, j;
        float px=0, py=0;
        ArrayList<PointF> pts;
        PointF p;

        // save current matrix
        canvas.save();

        // set parameter
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2.0f);

        // enumerate array of lines
        for(i=0;i<lines.size();i++){

            // get array of points
            pts = lines.get(i);

            // get the first point in an array
            if(pts.size() > 0){
                p = pts.get(0);
                px = p.x;
                py = p.y;
            }

            // enumerate array of points
            for(j=1;j<pts.size();j++){

                // get next point in an array
                p = pts.get(j);

                // draw lines
                canvas.drawLine(px, py, p.x, p.y, paint);

                // swap point
                px = p.x;
                py = p.y;
            }
        }

        // restore the matrix saved above
        canvas.restore();
    }

    // touch screen event
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        switch (action){
            case MotionEvent.ACTION_DOWN:         onActionDown(event);      break;
            case MotionEvent.ACTION_MOVE:         onActionMove(event);      break;
            default: return super.onTouchEvent(event);
        }

        return true;
    }

    // first down gesture
    private void onActionDown(MotionEvent event){
        int index;
        float x, y;

        // first point
        index = event.getActionIndex();
        x = event.getX(index);
        y = event.getY(index);

        // create array of points
        points = new ArrayList<PointF>();
        points.add(new PointF(x, y));

        // add array of points to array of lines
        lines.add(points);

        // refresh display
        invalidate();
    }

    // move gesture
    private void onActionMove(MotionEvent event){
        int index;
        float x, y;

        // next points as touch move
        index = event.getActionIndex();
        x = event.getX(index);
        y = event.getY(index);

        // add points to an array of points
        points.add(new PointF(x, y));

        // refresh display
        invalidate();
    }

    //
    public void clearArrayList(){

        int i;
        ArrayList<PointF> tmp;

        for(i=0;i<lines.size();i++){

            tmp = lines.get(i);
            tmp.clear();

        }

        lines.clear();
    }

    //
    public void clearScreen(){

        // release memory
        clearArrayList();

        // refresh display
        invalidate();
    }

    // refresh screen
    public void refresh(){
        invalidate();
    }
}
