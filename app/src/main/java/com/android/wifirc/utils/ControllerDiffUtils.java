package com.android.wifirc.utils;

import androidx.recyclerview.widget.DiffUtil;

import com.android.wifirc.room.entity.ControllerInfo;

import java.util.List;

public class ControllerDiffUtils extends DiffUtil.Callback {
    private final List<ControllerInfo> oldList;
    private final List<ControllerInfo> newList;

    public ControllerDiffUtils(List<ControllerInfo> oldList, List<ControllerInfo> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? -1 : oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null ? -1 : newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ControllerInfo item1 = oldItemPosition >= 0 && oldItemPosition < getOldListSize() ? oldList.get(oldItemPosition) : null;
        ControllerInfo item2 = newItemPosition >= 0 && newItemPosition < getNewListSize() ? newList.get(newItemPosition) : null;
        if (item1 == null || item2 == null) {
            return false;
        } else {
            return item1.equals(item2);
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ControllerInfo item1 = oldItemPosition >= 0 && oldItemPosition < getOldListSize() ? oldList.get(oldItemPosition) : null;
        ControllerInfo item2 = newItemPosition >= 0 && newItemPosition < getNewListSize() ? newList.get(newItemPosition) : null;
        if (item1 == null || item2 == null) {
            return false;
        } else {
            boolean isTimeAddedTheSame = item1.getTimeAdded().equals(item2.getTimeAdded());
            boolean isTitleTheSame = item1.getTitle().equals(item2.getTitle());
            return isTimeAddedTheSame && isTitleTheSame;
        }
    }
}
