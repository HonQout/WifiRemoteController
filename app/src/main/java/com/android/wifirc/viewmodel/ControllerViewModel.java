package com.android.wifirc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.wifirc.room.entity.ControllerInfo;

public class ControllerViewModel extends ViewModel {
    private final MutableLiveData<Integer> position = new MutableLiveData<Integer>();
    private final MutableLiveData<ControllerInfo> controllerInfo = new MutableLiveData<ControllerInfo>();
    private final MutableLiveData<String> host = new MutableLiveData<String>();
    private final MutableLiveData<Integer> port = new MutableLiveData<Integer>();

    public void setPosition(int position) {
        this.position.postValue(position);
    }

    public void setControllerInfo(ControllerInfo controllerInfo) {
        this.controllerInfo.postValue(controllerInfo);
    }

    public void setHost(String host) {
        this.host.postValue(host);
    }

    public void setPort(int port) {
        this.port.postValue(port);
    }

    public Integer getPosition() {
        return position.getValue();
    }

    public MutableLiveData<ControllerInfo> getControllerInfo() {
        return controllerInfo;
    }

    public MutableLiveData<String> getHost() {
        return host;
    }

    public MutableLiveData<Integer> getPort() {
        return port;
    }
}
