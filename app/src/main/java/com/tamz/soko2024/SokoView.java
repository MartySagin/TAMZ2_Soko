package com.tamz.soko2024;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by kru13
 */
public class SokoView extends View{

    Bitmap[] bmp;

    int lW = 10;
    int lH = 10;

    int pX = 6;
    int pY = 4;

    int width;
    int height;

    private int level[] = {
            1,1,1,1,1,1,1,1,1,0,
            1,0,0,0,0,0,0,0,1,0,
            1,0,2,3,3,2,1,0,1,0,
            1,0,1,3,2,3,2,0,1,0,
            1,0,2,3,3,2,4,0,1,0,
            1,0,1,3,2,3,2,0,1,0,
            1,0,2,3,3,2,1,0,1,0,
            1,0,0,0,0,0,0,0,1,0,
            1,1,1,1,1,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0
    };

    private int levelCopy[];

    public SokoView(Context context) {
        super(context);
        init(context);
    }

    public SokoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SokoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        bmp = new Bitmap[6];

        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), R.drawable.boxok);

        levelCopy = new int[lW*lH];
        System.arraycopy(level, 0, levelCopy, 0, lW*lH);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w / lW;
        height = h / lH;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private float startX;
    private float startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();

                return true;

            case MotionEvent.ACTION_UP:

                float endX = event.getX();
                float endY = event.getY();

                float deltaX = endX - startX;
                float deltaY = endY - startY;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0) {
                        //Toast.makeText(getContext(), "Swiped Right - Right Area", Toast.LENGTH_SHORT).show();

                        //Collisions
                        if (level[pY*lH + pX + 1] == 1) {
                            break;
                        }

                        //Collisions with boxes
                        if (level[pY*lH + pX + 1] == 2 || level[pY*lH + pX + 1] == 5) {
                            if (level[pY*lH + pX + 2] == 1 || level[pY*lH + pX + 2] == 2 || level[pY*lH + pX + 2] == 5) {

                                break;
                            }

                            if (level[pY*lH + pX] == 0 || level[pY*lH + pX] == 4) {
                                level[pY * lH + pX] = 0;
                            }

                            if (levelCopy[pY*lH + pX] == 3){
                                level[pY*lH + pX] = 3;
                            }

                            level[pY*lH + pX + 1] = 4;

                            //Change Box To Green If There Is A Cross
                            if (levelCopy[pY*lH + pX + 2] == 3){
                                level[pY*lH + pX + 2] = 5;
                            }else{
                                level[pY*lH + pX + 2] = 2;
                            }

                            pX++;

                            break;
                        }

                        //Under me was a cross
                        if (levelCopy[pY*lH + pX] == 3) {
                            level[pY*lH + pX] = 3;
                            level[pY*lH + pX + 1] = 4;

                            pX++;

                            break;
                        }

                        //Under me is nothing
                        if (level[pY*lH + pX] == 0) {
                            level[pY*lH + pX] = 0;
                            level[pY*lH + pX + 1] = 4;

                            pX++;

                            break;
                        }

                        //Next To Me Is Nothing Or Cross
                        if (level[pY*lH + pX + 1] == 0 || level[pY*lH + pX + 1] == 3){
                            level[pY*lH + pX] = 0;

                            level[pY*lH + pX + 1] = 4;

                            pX++;

                            break;
                        }



                    } else {
                        //Toast.makeText(getContext(), "Swiped Left - Left Area", Toast.LENGTH_SHORT).show();

                        //Collisions
                        if (level[pY*lH + pX - 1] == 1) {
                            break;
                        }

                        //Collisions with boxes
                        if (level[pY*lH + pX - 1] == 2 || level[pY*lH + pX - 1] == 5) {
                            if (level[pY*lH + pX - 2] == 1 || level[pY*lH + pX - 2] == 2 || level[pY*lH + pX - 2] == 5) {

                                break;
                            }

                            if (level[pY*lH + pX] == 0 || level[pY*lH + pX] == 4) {
                                level[pY * lH + pX] = 0;
                            }

                            if (levelCopy[pY*lH + pX] == 3){
                                level[pY*lH + pX] = 3;
                            }

                            level[pY*lH + pX - 1] = 4;

                            //Change Box To Green If There Is A Cross
                            if (levelCopy[pY*lH + pX - 2] == 3){
                                level[pY*lH + pX - 2] = 5;
                            }else{
                                level[pY*lH + pX - 2] = 2;
                            }

                            pX--;

                            break;
                        }

                        //Under me was a cross
                        if (levelCopy[pY*lH + pX] == 3) {
                            level[pY*lH + pX] = 3;
                            level[pY*lH + pX - 1] = 4;

                            pX--;

                            break;
                        }

                        //Under me is nothing
                        if (level[pY*lH + pX] == 0) {
                            level[pY*lH + pX] = 0;

                            level[pY*lH + pX - 1] = 4;

                            pX--;

                            break;
                        }

                        //Next To Me Is Nothing Or Cross
                        if (level[pY*lH + pX - 1] == 0 || level[pY*lH + pX - 1] == 3){
                            level[pY*lH + pX] = 0;

                            level[pY*lH + pX - 1] = 4;

                            pX--;

                            break;
                        }
                    }
                } else {
                    if (deltaY > 0) {
                        //Toast.makeText(getContext(), "Swiped Down - Bottom Area", Toast.LENGTH_SHORT).show();

                        //Collisions
                        if (level[(pY + 1)*lH + pX] == 1) {
                            break;
                        }

                        //Collisions with boxes
                        if (level[(pY + 1)*lH + pX] == 2 || level[(pY + 1)*lH + pX] == 5) {
                            if (level[(pY + 2)*lH + pX] == 1 || level[(pY + 2)*lH + pX] == 2 || level[(pY + 2)*lH + pX] == 5) {

                                break;
                            }

                            if (level[pY*lH + pX] == 0 || level[pY*lH + pX] == 4) {
                                level[pY * lH + pX] = 0;
                            }

                            if (levelCopy[pY*lH + pX] == 3){
                                level[pY*lH + pX] = 3;
                            }

                            level[(pY + 1)*lH + pX] = 4;

                            //Change Box To Green If There Is A Cross
                            if (levelCopy[(pY + 2)*lH + pX] == 3){
                                level[(pY + 2)*lH + pX] = 5;
                            }else{
                                level[(pY + 2)*lH + pX] = 2;
                            }

                            pY++;

                            break;
                        }

                        //Under me was a cross
                        if (levelCopy[pY*lH + pX] == 3) {
                            level[pY*lH + pX] = 3;
                            level[(pY + 1)*lH + pX] = 4;

                            pY++;

                            break;
                        }

                        //Under me is nothing
                        if (level[pY*lH + pX] == 0) {
                            level[pY*lH + pX] = 0;

                            level[(pY + 1)*lH + pX] = 4;

                            pY++;

                            break;
                        }

                        //Next To Me Is Nothing Or Cross
                        if (level[(pY + 1)*lH + pX] == 0 || level[(pY + 1)*lH + pX] == 3){
                            level[pY*lH + pX] = 0;

                            level[(pY + 1)*lH + pX] = 4;

                            pY++;

                            break;
                        }

                    } else {
                        //Toast.makeText(getContext(), "Swiped Up - Top Area", Toast.LENGTH_SHORT).show();

                        //Collisions
                        if (level[(pY - 1)*lH + pX] == 1) {
                            break;
                        }

                        //Collisions with boxes
                        if (level[(pY - 1)*lH + pX] == 2 || level[(pY - 1)*lH + pX] == 5) {
                            if (level[(pY - 2)*lH + pX] == 1 || level[(pY - 2)*lH + pX] == 2 || level[(pY - 2)*lH + pX] == 5) {

                                break;
                            }

                            if (level[pY*lH + pX] == 0 || level[pY*lH + pX] == 4) {
                                level[pY * lH + pX] = 0;
                            }

                            if (levelCopy[pY*lH + pX] == 3){
                                level[pY*lH + pX] = 3;
                            }

                            level[(pY - 1)*lH + pX] = 4;

                            //Change Box To Green If There Is A Cross
                            if (levelCopy[(pY - 2)*lH + pX] == 3){
                                level[(pY - 2)*lH + pX] = 5;
                            }else{
                                level[(pY - 2)*lH + pX] = 2;
                            }

                            pY--;

                            break;
                        }

                        //Under me was a cross
                        if (levelCopy[pY*lH + pX] == 3) {
                            level[pY*lH + pX] = 3;
                            level[(pY - 1)*lH + pX] = 4;

                            pY--;

                            break;
                        }

                        //Under me is nothing
                        if (level[pY*lH + pX] == 0) {
                            level[pY*lH + pX] = 0;

                            level[(pY - 1)*lH + pX] = 4;

                            pY--;

                            break;
                        }

                        //Next To Me Is Nothing Or Cross
                        if (level[(pY - 1)*lH + pX] == 0 || level[(pY - 1)*lH + pX] == 3){
                            level[pY*lH + pX] = 0;

                            level[(pY - 1)*lH + pX] = 4;

                            pY--;

                            break;
                        }
                    }
                }

        }

        invalidate();

        return super.onTouchEvent(event);
    }

    boolean finishedLevel = false;

    //@SuppressLint("DrawAllocation")
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                canvas.drawBitmap(bmp[level[y*lW + x]], null,
                        new Rect(x*width,
                                y*height,
                                (x+1)*width,
                                (y+1)*height), null);
            }
        }

        if (CheckIfLevelIsComplete() && !finishedLevel){
            Toast.makeText(getContext(), "FINISHED!!!!", Toast.LENGTH_SHORT).show();

            finishedLevel = true;
        }
    }

    boolean CheckIfLevelIsComplete(){
        boolean finished = true;

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                if (level[y*lH + x] == 2){
                    finished = false;
                }
            }
        }

        return finished;
    }
}
