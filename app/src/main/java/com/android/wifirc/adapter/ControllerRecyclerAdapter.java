package com.android.wifirc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.android.wifirc.R;
import com.android.wifirc.room.entity.ControllerInfo;
import com.android.wifirc.utils.ControllerDiffUtils;

import java.util.List;

public class ControllerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<ControllerInfo> infoList;
    private ItemClickListener itemClickListener;

    public ControllerRecyclerAdapter(Context context, List<ControllerInfo> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    public void setInfoList(List<ControllerInfo> infoList) {
        List<ControllerInfo> oldList = this.infoList;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ControllerDiffUtils(oldList, infoList));
        diffResult.dispatchUpdatesTo(this);
        this.infoList = infoList;
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener=itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_controller, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.item_title.setText(infoList.get(position).getTitle());
        itemHolder.item_description.setText(infoList.get(position).getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemClickListener.onItemLongClick(holder.getAdapterPosition());
                return true;
            }
        });
    }

    public List<ControllerInfo> getControllerList() {
        return infoList;
    }

    @Override
    public int getItemCount() {
        return infoList == null ? -1 : infoList.size();
    }

    public boolean deleteItem(int position) {
        if (position < 0 || position > infoList.size()) {
            return false;
        }
        boolean lastOne = position + 1 == infoList.size();
        infoList.remove(position);
        notifyItemRemoved(position);
        if (!lastOne) {
            if (position != infoList.size()) {
                notifyItemRangeChanged(position, infoList.size() - position);
            } else {
                notifyItemRangeChanged(position, position);
            }
        }
        return true;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        public TextView item_title;
        public TextView item_description;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_description = itemView.findViewById(R.id.item_description);
        }
    }

    public interface ItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }
}
