package com.game.chess.model;

import com.game.chess.R;
import com.game.chess.spar.MainActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	private final SparseArray<View> mViews;  
    private int mPosition;  
    private View mConvertView;  
    private Context context;
  
    private ViewHolder(Context context, ViewGroup parent, int layoutId,  
            int position)  
    {  
    	this.context = context;
        this.mPosition = position;  
        this.mViews = new SparseArray<View>();  
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,  
                false);  
        // setTag  
        mConvertView.setTag(this);  
    }  
  
    /** 
     * 拿到一个ViewHolder对象 
     *  
     * @param context 
     * @param convertView 
     * @param parent 
     * @param layoutId 
     * @param position 
     * @return 
     */  
    public static ViewHolder get(Context context, View convertView,  
            ViewGroup parent, int layoutId, int position)  
    {  
        if (convertView == null)  
        {  
        	
        	ViewHolder holder = new ViewHolder(context, parent, layoutId, position); 

        	// TODO Auto-generated method stub
			int[] xy = ((MainActivity)context).postion2XY(position);
			final ImageView boardImageView = holder.getView(R.id.boardImageView);
            //棋盘4角
            if(xy[0] == 0 && xy[1] == 0){
                //左上角
            	boardImageView.setImageResource(R.drawable.top_left);
            }

            if(xy[0] == 0 && xy[1] == 13){
                //右上角
                boardImageView.setImageResource(R.drawable.top_right);
            }

            if(xy[0] == 15 && xy[1] == 0){
                //左下角
                boardImageView.setImageResource(R.drawable.bottom_left);
            }

            if(xy[0] == 15 && xy[1] == 13){
                //右下角
                boardImageView.setImageResource(R.drawable.bottom_right);
            }

            if(xy[0] == 0 && xy[1] != 0 && xy[1] != 13){
                //上边缘
                boardImageView.setImageResource(R.drawable.top);
            }

            if(xy[0] == 15 && xy[1] != 0 && xy[1] != 13){
                //下边缘
                boardImageView.setImageResource(R.drawable.bottom);
            }

            if(xy[1] == 0 && xy[0] != 0 && xy[0] != 15){
                //左边缘
                boardImageView.setImageResource(R.drawable.left);
            }

            if(xy[1] == 13 && xy[0] != 0 && xy[0] != 15){
                //右边缘
                boardImageView.setImageResource(R.drawable.right);
            }

            if(xy[0] > 0 && xy[0] < 15 && xy[1] > 0 && xy[1] < 13){
                boardImageView.setImageResource(R.drawable.line);
            }
            
            return holder;  
        }  
        return (ViewHolder) convertView.getTag();  
    }  
  
    public View getConvertView()  
    {  
        return mConvertView;  
    }  
  
    /** 
     * 通过控件的Id获取对于的控件，如果没有则加入views 
     *  
     * @param viewId 
     * @return 
     */  
    public <T extends View> T getView(int viewId)  
    {  
        View view = mViews.get(viewId);  
        if (view == null)  
        {  
            view = mConvertView.findViewById(viewId);  
            mViews.put(viewId, view);  
        }  
        return (T) view;  
    }  
  
    /** 
     * 为TextView设置字符串 
     *  
     * @param viewId 
     * @param text 
     * @return 
     */  
    public ViewHolder setText(int viewId, String text)  
    {  
        TextView view = getView(viewId);  
        view.setText(text);  
        return this;  
    }  
    
    /** 
     * 为图标TextView设置调用机制 change by ct
     *  
     * @param viewId 
     * @param text 
     * @return 
     */  
    public ViewHolder setText(int viewId, Typeface iconFont2)  
    {  
        TextView view = getView(viewId);  
        view.setTypeface(iconFont2);
        return this;  
    }  
  
    /** 
     * 为ImageView设置图片 
     *  
     * @param viewId 
     * @param drawableId 
     * @return 
     */  
    public ViewHolder setImageResource(int viewId, int drawableId)  
    {  
        ImageView view = getView(viewId);  
        view.setImageResource(drawableId);  
  
        return this;  
    }  
  
    /** 
     * 为ImageView设置图片 
     *  
     * @param viewId 
     * @param drawableId 
     * @return 
     */  
    public ViewHolder setImageBitmap(int viewId, Bitmap bm)  
    {  
        ImageView view = getView(viewId);  
        view.setImageBitmap(bm);  
        return this;  
    }  
  
  
    public int getPosition()  
    {  
        return mPosition;  
    }  
  
}