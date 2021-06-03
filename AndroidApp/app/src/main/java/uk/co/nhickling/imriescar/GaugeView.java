package uk.co.nhickling.imriescar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {

    private Paint arcPaint;

    Color majorMarkerColor;
    Color minorMarkerColor;
    Color lowBarColor;
    Color medBarColor;
    Color highBarColor;

    float medTransitionPoint;
    float highTransitionPoint;

    float minValue;
    float maxValue;
    float curValue;

    String unitOfMeasure;

    public GaugeView(Context context) {
        super(context);
        initialize(context, null);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(15f);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GaugeView, 0, 0);

        try{
            this.majorMarkerColor = Color.valueOf(a.getColor(R.styleable.GaugeView_majorMarkerColor, Color.DKGRAY));
            this.minorMarkerColor = Color.valueOf(a.getColor(R.styleable.GaugeView_minorMarkerColor, Color.GRAY));
            this.lowBarColor = Color.valueOf(a.getColor(R.styleable.GaugeView_lowBarColor, Color.RED));
            this.medBarColor = Color.valueOf(a.getColor(R.styleable.GaugeView_medBarColor, Color.YELLOW));
            this.highBarColor = Color.valueOf(a.getColor(R.styleable.GaugeView_highBarColor, Color.GREEN));

            this.medTransitionPoint = a.getFloat(R.styleable.GaugeView_medTransitionPerc, 11.3f);
            this.highTransitionPoint = a.getFloat(R.styleable.GaugeView_highTransitionPerc, 11.6f);

            this.minValue = a.getFloat(R.styleable.GaugeView_minValue, 11.1f);
            this.maxValue = a.getFloat(R.styleable.GaugeView_maxValue, 12.6f);
            this.curValue = a.getFloat(R.styleable.GaugeView_currentValue, 11.412345648f);

            this.unitOfMeasure = a.getString(R.styleable.GaugeView_unitOfMeasure);
            if(this.unitOfMeasure == null) this.unitOfMeasure = "";
        } finally{
            a.recycle();
        }

        if(attrs != null){

        }
        else{

        }

    }

    public void SetValue(float newValue){
        this.curValue = newValue;
        this.invalidate();
    }

    private float valueToPercentage(float value){
        float trimmedValue = value;
        if(trimmedValue < this.minValue) trimmedValue = this.minValue;
        else if(trimmedValue > this.maxValue) trimmedValue = this.maxValue;
        trimmedValue -= this.minValue;
        float perc = trimmedValue / (this.maxValue - this.minValue);
        perc *= 100;
        return perc;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int leftMargin = width / 8;
        int topMargin = height / 8;


        int startX = leftMargin;
        int startY = height - topMargin;
        float perc = valueToPercentage(this.curValue);
        canvas.rotate(90f * (perc / 100f), width - leftMargin,height - topMargin);
        arcPaint.setShadowLayer(3.0f,0f,0f, Color.BLACK);
        arcPaint.setColor(Color.WHITE);
        canvas.drawLine(width - leftMargin, startY, startX * 1.4f, startY, arcPaint);
        arcPaint.setColor(Color.CYAN);
        canvas.drawLine(startX * 1.5f, startY, startX * 1.8f, startY, arcPaint);
        canvas.rotate(-90f * (perc / 100f), width - leftMargin,height - topMargin);

        if(this.curValue > highTransitionPoint){
            arcPaint.setColor(this.highBarColor.toArgb());
        } else if(this.curValue > medTransitionPoint){
            arcPaint.setColor(this.medBarColor.toArgb());
        } else{
            arcPaint.setColor(this.lowBarColor.toArgb());
        }
        arcPaint.setStrokeWidth(3);
        arcPaint.setTextSize(45);
        arcPaint.setTextAlign(Paint.Align.RIGHT);
        arcPaint.setStyle(Paint.Style.FILL);
        arcPaint.setShadowLayer(1.0f,0f,0f, Color.BLACK);
        String value = String.format("%.2f",this.curValue) + this.unitOfMeasure;
        canvas.drawText(value, (int)(width * 0.75),(int)(height * 0.95), arcPaint);

        // Draw the pointers
        final int totalNoOfPointers = 20;
        final int pointerMaxHeight = 25;
        final int pointerMinHeight = 15;

        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        float highPerc = valueToPercentage(this.highTransitionPoint);
        float medPerc = valueToPercentage(this.medTransitionPoint);

        int highMarkTransition = (int)(highPerc / (100 / totalNoOfPointers));
        int medMarkTransition = (int)(medPerc / (100 / totalNoOfPointers));

        int pointerHeight;
        for (int i = 0; i <= totalNoOfPointers; i++) {
            if(i%5 == 0){
                pointerHeight = pointerMaxHeight;
            }else{
                pointerHeight = pointerMinHeight;
            }
            if( i > highMarkTransition){
                arcPaint.setColor(this.highBarColor.toArgb());
            }
            else if( i > medMarkTransition){
                arcPaint.setColor(this.medBarColor.toArgb());
            }
            else{
                arcPaint.setColor(this.lowBarColor.toArgb());
            }
            canvas.drawLine(startX, startY, startX - pointerHeight, startY, arcPaint);
            canvas.rotate(90f/totalNoOfPointers, width - leftMargin,height - topMargin);
        }

    }
}
