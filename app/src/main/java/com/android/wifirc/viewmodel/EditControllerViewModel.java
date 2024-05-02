package com.android.wifirc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EditControllerViewModel extends ViewModel {
    private final MutableLiveData<Integer> option = new MutableLiveData<Integer>();
    private final MutableLiveData<Integer> position = new MutableLiveData<Integer>();

    public void setOption(int option) {
        this.option.postValue(option);
    }

    public void setPosition(int position) {
        this.position.postValue(position);
    }

    public Integer getOption() {
        return option.getValue();
    }

    public Integer getPosition() {
        return position.getValue();
    }
}
