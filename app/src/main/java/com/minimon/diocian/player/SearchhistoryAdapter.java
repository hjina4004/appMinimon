package com.minimon.diocian.player;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class SearchhistoryAdapter extends RecyclerView.Adapter {
    private List<SearchItem> items;
    private Context mContext;
    private DBHelper dbHelper;
    private SearchHistoryAdapterListener mListener;

    public interface SearchHistoryAdapterListener{
        void onClick();
    }

    public void setHistorySearchListener(SearchHistoryAdapterListener listener){
        mListener = listener;
    }

    public SearchhistoryAdapter(Context context,  List<SearchItem> modelData){
        this.items = modelData;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_history,parent,false);
        dbHelper = new DBHelper(mContext.getApplicationContext(), "SearchLog.db",null,1);
        return new SearchHistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SearchItem item = items.get(position);
        SearchHistoryViewHolder itemHolder = (SearchHistoryViewHolder)holder;
        itemHolder.tv_history.setText(item.getHistory());
        itemHolder.tv_date.setText(item.getDate());
        itemHolder.img_delete_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.delete(items.get(position).getHistory());
                items.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SearchHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv_history;
        TextView tv_date;
        ImageView img_delete_history;
        public SearchHistoryViewHolder(View itemView){
            super(itemView);
            tv_history = itemView.findViewById(R.id.tv_history);
            tv_date = itemView.findViewById(R.id.tv_date);
            img_delete_history = itemView.findViewById(R.id.img_delete_history);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener!=null) mListener.onClick();
        }
    }
}
