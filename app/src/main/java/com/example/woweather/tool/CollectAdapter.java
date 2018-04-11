package com.example.woweather.tool;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woweather.R;
import com.example.woweather.db.Collection;

import java.util.List;

/**
 * Created by 邹永鹏 on 2018/4/8.
 */

public class CollectAdapter extends RecyclerView.Adapter<CollectAdapter.ViewHolder> {
    private List<Collection>  mCollectionList;

    private OnItemClickListener mOnItemClickListener;
    //在里面实现具体的点击响应事件，同时传入两个参数：view和postion
    public interface OnItemClickListener{
        void onItemClick(String weatherId);

        void onItemDelete(String weatherId);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener=onItemClickListener;
    }

    //定义内部类
    class ViewHolder extends RecyclerView.ViewHolder{
        View collectView;
        TextView holderName;
        Button deleteButton;

        public ViewHolder(View view){
            super(view);
            collectView=view;
            holderName=(TextView) view.findViewById(R.id.collect_name);
            deleteButton=(Button) view.findViewById(R.id.delete_button);
        }
    }

    public CollectAdapter(List<Collection> collectionList){
        mCollectionList=collectionList;
    }

    //创建ViewHolder实例，把collect_item传入构造函数中
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collect_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        //添加点击事件
        holder.collectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                Collection collection=mCollectionList.get(position);
                Log.d("LOCAL","you click "+collection.getCollectName()+" - "+collection.getCollectId());
                Toast.makeText(view.getContext(),"you click "+collection.getCollectName()+" - "+collection.getCollectId(),Toast.LENGTH_SHORT).show();
                mOnItemClickListener.onItemClick(collection.getCollectId());
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                Collection collection=mCollectionList.get(position);
                Toast.makeText(view.getContext(),"you delete "+collection.getCollectName()+" - "+collection.getCollectId(),Toast.LENGTH_SHORT).show();
                mOnItemClickListener.onItemDelete(collection.getCollectId());
            }
        });
        return holder;
    }

    ////对item控件进行点击事件的监听并回调给自定义的监听
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Collection collection=mCollectionList.get(position);
        holder.holderName.setText(collection.getCollectName());
    }

    @Override
    public int getItemCount() {
        return mCollectionList.size();
    }
}
