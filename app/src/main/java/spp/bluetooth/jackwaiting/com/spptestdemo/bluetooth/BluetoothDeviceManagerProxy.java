package spp.bluetooth.jackwaiting.com.spptestdemo.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import spp.bluetooth.jackwaiting.lib.listeners.OnBluetoothDeviceConnectionStateChangedListener;
import spp.bluetooth.jackwaiting.lib.listeners.OnBluetoothDeviceDiscoveryListener;
import spp.bluetooth.jackwaiting.lib.listeners.OnBluetoothDeviceGlobalUIChangedListener;
import spp.bluetooth.jackwaiting.lib.listeners.OnBluetoothDeviceManagerReadyListener;
import spp.bluetooth.jackwaiting.lib.managers.BluetoothDeviceManager;
import spp.bluetooth.jackwaiting.lib.status.ConnectionState;
import spp.bluetooth.jackwaiting.lib.status.ConnectionType;

public class BluetoothDeviceManagerProxy {
    public static final String TAG = "BluetoothDeviceManager";
    public static BluetoothDeviceManagerProxy proxy;
    /**
     * 是否已连接
     */
    private boolean connected;
    /**
     * 蓝牙设备管理类
     */
    private static BluetoothDeviceManager bluzDeviceMan;
    /**
     * 已连接的蓝牙设备
     */
    private BluetoothDevice connectedDevice;
    /**
     * 连接状态监听集合
     */
    private List<ConnectionListener> conStateListeners;
    /**
     * 连接状态监听集合
     */
    private List<OnBluetoothDeviceConnectionStateChangedListener> conStateListener;

    /**
     * 蓝牙地址过滤
     */
    private static final String MAC_ADDRESS_FILTER_PREFIX = "";

    private int bluetoothDeviceNameTypes = 0;
    private boolean modifieds = false;
    private Context context;

    private static int currentDeviceMode;

    private BluetoothDeviceManagerProxy(Context context) {
        this.context = context.getApplicationContext();
        getBluetoothDeviceManager();
        conStateListeners = new ArrayList<ConnectionListener>();
        conStateListener = new ArrayList<OnBluetoothDeviceConnectionStateChangedListener>();// 初始化连接状态监听的集合
    }

    public static BluetoothDeviceManagerProxy getInstance(Context context) {
        if (proxy == null) {
            proxy = new BluetoothDeviceManagerProxy(context.getApplicationContext());
        }
        return proxy;
    }

    public Context getContext() {
        return context;
    }

    /**
     * 获取已存在的实例对象，如果不存在则返回null
     *
     * @return
     */
    public static BluetoothDeviceManagerProxy getInstance() {
        return proxy;
    }

    private boolean bluzManReady;

    /**
     * 获取蓝牙管理类
     *
     * @return
     */
    public BluetoothDeviceManager getBluetoothDeviceManager() {
        if (bluzDeviceMan == null) {
            Log.d("", "getBluetoothDeviceManager");
            bluzManReady = false;
            bluzDeviceMan = BluetoothDeviceManager.getInstance(context);
            //bluzDeviceMan.setConnectType(ConnectionType.ONLY_A2DP);
            bluzDeviceMan
                    .setOnBluetoothDeviceConnectionStateChangedListener(connStateChangeListener);

            bluzDeviceMan.setOnBluetoothDeviceManagerReadyListener(new OnBluetoothDeviceManagerReadyListener() {
                @Override
                public void onBluetoothDeviceManagerReady() {
                    Log.d("ManagerReady", "onBluetoothDeviceManagerReady");
                    bluzManReady = true;
                    bluzDeviceMan
                            .setOnBluetoothDeviceGlobalUIChangedListener(globalUiChangedListener);

                }
            });
            bluzDeviceMan.build();
        }
        return bluzDeviceMan;
    }

    /**
     * 蓝牙设备修改管理类监听
     */
    public interface OnBluzDeviceNameReadyListener {

        /**
         * 已准备好的回调
         *
         * @param i    修改类型
         * @param flag 是否修改成功
         */
        void OnBluzDeviceNameReadyListener(int i, boolean flag);
    }

    private OnBluzDeviceNameReadyListener bluzDeviceNameReadyListener;

    /**
     * 获取蓝牙设备修改管理类
     *
     * @param listener
     */
    public void getBluzDeviceNameManager(OnBluzDeviceNameReadyListener listener) {
        bluzDeviceNameReadyListener = listener;
    }


    /**
     * 获取设备的模式
     *
     * @return
     */
    public int getBluetoothManagerMode() {
        return currentDeviceMode;
    }


    private OnBluetoothDeviceDiscoveryListener remoteDiscoveryListener;

    public void setDiscoveryListener(
            OnBluetoothDeviceDiscoveryListener discoveryListener) {
        this.remoteDiscoveryListener = discoveryListener;
    }

    public void removeDiscoveryListener(OnBluetoothDeviceDiscoveryListener discoveryListener) {
        if (discoveryListener == remoteDiscoveryListener) {
            remoteDiscoveryListener = null;
        }
    }

    /**
     * 扫描蓝牙设备
     *
     * @param listener 监听
     */
    public void startDiscovery(OnBluetoothDeviceDiscoveryListener listener) {
        Log.i("startDiscoverys", "startDiscoverys");
        bluzDeviceMan = getBluetoothDeviceManager();
        bluzDeviceMan.setOnBluetoothDeviceDiscoveryListener(listener);
        bluzDeviceMan.startDiscovery();
    }

    public void cacelDiscovery() {
        if (bluzDeviceMan.isDiscovering()) {
            bluzDeviceMan.cancelDiscovery();
        }
    }

    private boolean connecting;

    public boolean isConnecting() {
        return connecting;
    }

    /**
     * 连接蓝牙
     *
     * @param device 调用该方法之前是否已经连接了此设备
     * @return
     */
    public boolean connectDevice(BluetoothDevice device) {
        Log.i(TAG, "connectDevice = " + device);
        connecting = true;
        BluetoothDevice sppConnectedDevice = null;
        bluzDeviceMan = getBluetoothDeviceManager();
        sppConnectedDevice = bluzDeviceMan.getBluetoothDeviceConnected();
        if ((sppConnectedDevice != null)
                && (sppConnectedDevice.getAddress() != null)
                && (sppConnectedDevice.getAddress().equals(device.getAddress()))) {
            connecting = false;
            connectedDevice = device;
            return true;
        } else {
            Log.i(TAG, "开始执行连接");
            bluzDeviceMan.connect(device);
        }
        return false;
    }

    /**
     * 获取已连接的蓝牙设备
     *
     * @return 已连接的蓝牙设备
     */
    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * 连接默认设备, 扫描到mac地址符合的设备会自动连接
     */
    public void connect() {
        notifyConntectionStateChanged(null, CONN_STATE_START);
        startDiscovery(discoveryListener);
    }

    /**
     * 开始连接
     */
    public static final int CONN_STATE_START = -1;
    public static final int NO_DISCOVERY_DEVICE = -2;    //没有发现设备
    public static final int REPLAY_CONTENT_DEVICE = -3;    //回连失败

    /**
     * 连接状态监听
     */
    public interface ConnectionListener {
        void onConnectStateChanged(int state);
    }

    /**
     * 添加连接状态改变监听
     *
     * @param listener
     */
    public void addOnBluetoothDeviceConnectionStateChangedListener(
            ConnectionListener listener) {
        if (conStateListeners.contains(listener)) {
            conStateListeners.remove(listener);
        }
        conStateListeners.add(listener);
    }

    public void removeOnBluetoothDeviceConnectionStateChangedListener(
            ConnectionListener listener) {
        conStateListeners.remove(listener);
    }

    public void addOnBluetoothDeviceConnectionStateChangedListener(
            OnBluetoothDeviceConnectionStateChangedListener listener) {
        conStateListener.add(listener);
    }

    public void removeOnBluetoothDeviceConnectionStateChangedListener(
            OnBluetoothDeviceConnectionStateChangedListener listener) {
        conStateListener.remove(listener);
    }

    /**
     * 通知监听器，蓝牙连接状态改变
     *
     * @param device
     * @param state
     */
    private void notifyConntectionStateChanged(BluetoothDevice device, int state) {

        int size = conStateListeners.size();
        for (int i = 0; i < size; i++) {
            conStateListeners.get(i).onConnectStateChanged(state);
        }
        int sizes = conStateListener.size();
        for (int i = 0; i < sizes; i++) {
            conStateListener.get(i).onBluetoothDeviceConnectionStateChanged(
                    device, state);
        }
    }

    /**
     * 断开蓝牙连接
     */
    public void disconnected() {
        if (bluzDeviceMan != null && connected) {
            bluzDeviceMan.disconnect(connectedDevice);
        }
    }

    private boolean discoverying;

    public boolean isDiscoverying() {
        return discoverying;
    }

    private int currentVolume;


    /**
     * 获取设备音量
     *
     * @return
     */
    public int getCurrentVolume() {
        return currentVolume;
    }

    public interface OnDeviceUiChangedListener {
        void onVolumeChanged(boolean firstCallback, int volume, boolean on);
    }

    public static class SimpleDeviceUiChangedListener implements OnDeviceUiChangedListener {
        @Override
        public void onVolumeChanged(boolean firstCallback, int volume, boolean on) {
        }
    }

    private OnDeviceUiChangedListener mDeviceUiChangedListener;

    /**
     * 添加设备UI相关变化监听，如音量
     * 注：退出界面时，调用removeDeviceUiChangedListener
     *
     * @param listener
     */
    public void setDeviceUiChangedListener(OnDeviceUiChangedListener listener) {
        mDeviceUiChangedListener = listener;
        if (mDeviceUiChangedListener != null) {
            mDeviceUiChangedListener.onVolumeChanged(true, currentVolume, true);
        }
    }

    public void removeDeviceUiChangeListener(OnDeviceUiChangedListener listener) {
        if (mDeviceUiChangedListener == listener) {
            mDeviceUiChangedListener = null;
        }
    }

    private BluetoothDevice targetDevice;

    private String targetDeviceName;


    /**
     * 获取搜索到的目标设备
     *
     * @return
     */
    public BluetoothDevice getTargetDevice() {
        return targetDevice;
    }

    /**
     * 获取搜索到的目标名称
     *
     * @return
     */
    public String getTargetDeviceName() {
        return targetDeviceName;
    }

    private OnBluetoothDeviceDiscoveryListener discoveryListener = new OnBluetoothDeviceDiscoveryListener() {

        @Override
        public void onBluetoothDeviceDiscoveryFinished() {
            discoverying = false;
            if (targetDevice == null && !isConnected() && bluzDeviceMan.getBluetoothDeviceConnectedA2dp() == null) {
                notifyConntectionStateChanged(targetDevice, NO_DISCOVERY_DEVICE);
            }
            if (remoteDiscoveryListener != null) {
                remoteDiscoveryListener.onBluetoothDeviceDiscoveryFinished();
            }
        }

        @Override
        public void onBluetoothDeviceDiscoveryFound(BluetoothDevice device) {
            Log.i("bluetooth", device.getName() + "," + device.getAddress());
            String address = device.getAddress();
            targetDevice = device;

            if (bluzDeviceMan.getBluetoothDeviceConnectedA2dp() == null) {
                //连接之前最近连过的设备

                    /*if(PreferenceUtil.getCurrentDevice().equals(device.getAddress())){
                        PreferenceUtil.setCurrentDevice(device.getAddress());
                        connectDevice(device);
                    }*/
                   /* if (CustomApplication.deviced == null) {
                        CustomApplication.deviced = device;
                        connectDevice(device);
                    } else {
                        connectDevice(CustomApplication.deviced);
                    }*/

                targetDeviceName = device.getName();
            }
            if (remoteDiscoveryListener != null) {
                remoteDiscoveryListener.onBluetoothDeviceDiscoveryFound(device);
            }
        }

        @Override
        public void onBluetoothDeviceDiscoveryStarted() {
            discoverying = true;
            targetDevice = null;
            if (remoteDiscoveryListener != null) {
                remoteDiscoveryListener.onBluetoothDeviceDiscoveryStarted();
            }
        }
    };

    public int  getA2dpConnectState(){
        int a2dpState = ConnectionState.INIT_CONNECT;
        if (bluzDeviceMan != null) {
            a2dpState = bluzDeviceMan.getA2dpConnectState();
        }
        return a2dpState;
    }

    /**
     * <p>
     * 获取hfp连接状态
     * </p>
     *
     * @since 1.0.0
     */
    public int getHfpConnectState(){
        int hfpState = ConnectionState.INIT_CONNECT;
        if (bluzDeviceMan != null) {
            hfpState = bluzDeviceMan.getHfpConnectState();
        }
        return hfpState;
    }


    /**
     * 蓝牙连接状态改变监听
     */
    private OnBluetoothDeviceConnectionStateChangedListener connStateChangeListener = new OnBluetoothDeviceConnectionStateChangedListener() {
        @Override
        public void onBluetoothDeviceConnectionStateChanged(
                BluetoothDevice device, int state) {
            if(device != null){
                Log.i("ManagerProxyAddress", device + "," + device.getAddress() + "," + state + "");
            }
            connectedDevice = device;
            switch (state) {
                case ConnectionState.CONNECTED:
                    connected = true;
                    targetDeviceName = device.getName();
                    if (onDeviceRefreshDateListener != null) {
                        onDeviceRefreshDateListener.onDeviceRefreshDate(state);
                    }
                    break;
                case ConnectionState.SPP_DISCONNECTED:
                case ConnectionState.A2DP_DISCONNECTED:
                case ConnectionState.DISCONNECTED:
                    Log.i("ManagerProxy", "进来了！");
                    connected = false;
                    if (onDeviceRefreshDateListener != null) {
                        onDeviceRefreshDateListener.onDeviceRefreshDate(state);
                    }
                    break;
            }
            connecting = false;
            notifyConntectionStateChanged(device, state);
        }
    };

    private static onDeviceRefreshDateListener onDeviceRefreshDateListener;

    public interface onDeviceRefreshDateListener {
        void onDeviceRefreshDate(int state);
    }

    /**
     * 设置获取设备信息
     */
    public static void setDeviceRefreshDateListener(onDeviceRefreshDateListener DeviceRefreshDateListener) {
        onDeviceRefreshDateListener = DeviceRefreshDateListener;
    }

    /**
     * 移除获取设备信息
     */
    public static void removeDeviceRefreshDateListener() {
        onDeviceRefreshDateListener = null;
    }

    private List<OnBluetoothDeviceGlobalUIChangedListener> globalUIChangedListeners = new ArrayList<>();


    /**
     * 添加频道切换的UI监听
     */
    public void addOnBluetoothDeviceGlobalUIChangedListener(
            OnBluetoothDeviceGlobalUIChangedListener listener) {
        if (!globalUIChangedListeners.contains(listener)) {
            globalUIChangedListeners.add(listener);
        }
    }

    public void removeOnBluetoothDeviceGlobalUIChangeListener(OnBluetoothDeviceGlobalUIChangedListener listener) {
        globalUIChangedListeners.remove(listener);
    }

    private OnBluetoothDeviceGlobalUIChangedListener globalUiChangedListener = new OnBluetoothDeviceGlobalUIChangedListener() {


        @Override
        public void onBluetoothDeviceChannelPre() {

        }

        @Override
        public void onBluetoothDeviceChannelNext() {

        }

        @Override
        public void onBluetoothDeviceOpenApplication() {

        }

        @Override
        public void onBluetoothDeviceMusicNext() {

        }

        @Override
        public void onBluetoothDeviceMusicPre() {

        }

        @Override
        public void onBluetoothDeviceSeekToStart() {

        }

        @Override
        public void onBluetoothDeviceSeekToStop() {

        }
    };

    public void destoryBluzDeviceMan() {
        if (bluzDeviceMan != null) {
            bluzDeviceMan = null;
        }
    }

    /**
     * 释放蓝牙，播放器等资源
     * 注意：需要在退出程序的最后执行，否则处于其之后的代码不会执行
     */
    public void destory() {
        conStateListeners.clear();
        conStateListener.clear();
        //disconnected();
        bluzManReady = false;
        proxy = null;
        // discoveryListener =null;
        remoteDiscoveryListener = null;
        if (bluzDeviceMan != null) {
            if (bluzDeviceMan.isDiscovering()) {
                bluzDeviceMan.cancelDiscovery();
            }
            Log.d("ManagerProxy", "bluzDeviceMan.release();");
            //放在最后，release()方法会调用System.exist(),处在它之后的代码不能执行
            bluzDeviceMan.release();
            System.exit(0);
        }
    }


}