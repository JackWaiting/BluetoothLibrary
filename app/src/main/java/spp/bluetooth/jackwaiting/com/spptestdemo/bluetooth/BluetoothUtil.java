package spp.bluetooth.jackwaiting.com.spptestdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;

public class BluetoothUtil {
	/**
	 * 蓝牙是否开启
	 * @return
	 */
	public static boolean isBluetoothEnable() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			return false;
		}
		return mBluetoothAdapter.isEnabled();
	}
	
	/**
	 * 开启蓝牙
	 * @return 如果设备不支持蓝牙，返回false 
	 */
	public static boolean enableBluetooth() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			return false;
		}
		if(!mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.enable();
		}
		return true;
	}
	/**
	 * 关闭蓝牙
	 */
	public static void disableBluetooth(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			return;
		}
		if(mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.disable();
		}
	}
}
