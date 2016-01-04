package com.ksy.media.widget.util;

import java.util.ArrayList;
import java.util.List;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetReceiver extends BroadcastReceiver {

	 /**
     * 枚举网络状态
     * NET_NO：没有网络 ,  NET_2G:2g网络 , NET_3G：3g网络 ,NET_4G：4g网络 ,NET_WIFI：wifi , NET_UNKNOWN：未知网络 
     */
    public static enum NetState{NET_NO,NET_2G,NET_3G,NET_4G,NET_WIFI,NET_UNKNOWN};
    
    public static IntentFilter intentFilter = new IntentFilter( "android.net.conn.CONNECTIVITY_CHANGE" ); 


    
    private List<NetStateChangedListener> mListeners ;

	private NetReceiver(){
		mListeners= new ArrayList<NetStateChangedListener>();
	}
	private static NetReceiver mInstance;
	
	public static NetReceiver getInstance(){
		if(mInstance == null)
			mInstance = new NetReceiver();
		return mInstance;
	}
	
	public void registNetBroadCast(Context context){
		Log.i(Constants.LOG_TAG, "registNetBroadCast");
		context.registerReceiver(this, intentFilter);
	}
	
	public void unRegistNetBroadCast(Context context){
		Log.i(Constants.LOG_TAG, "unRegistNetBroadCast");
		if(mListeners.size() > 0){
			Log.i(Constants.LOG_TAG,"there are other listeners , reject this request");
			return;
		}
		context.unregisterReceiver(this);
	}
	
	
	public void addNetStateChangeListener(NetStateChangedListener listener){
		if(listener == null)
			return ;
		if(mListeners.contains(listener))
			return ;
		mListeners.add(listener);
	}
	
	public void remoteNetStateChangeListener(NetStateChangedListener listener){
		if(listener == null)
			return ;
		if(mListeners.contains(listener))
			mListeners.remove(listener);
	}
	
	public void clearNetStateChangeListeners(){
		mListeners.clear();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		for(NetStateChangedListener listener : mListeners){
			listener.onNetStateChanged(getCurrentNetStateCode(context));
		}
	}
	
	public NetState getCurrentNetStateCode(Context context) {
        NetState stateCode = NetState.NET_NO;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {
            	//wifi 
                case ConnectivityManager.TYPE_WIFI:
                     stateCode = NetState.NET_WIFI;  
                    break;
                //mobile 网络
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: //联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: //电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: //移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager. NETWORK_TYPE_IDEN:
                           stateCode = NetState.NET_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: //电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            stateCode = NetState.NET_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE://4G
                            stateCode = NetState.NET_4G;
                            break;
                        //未知,一般不会出现
                        default:
                            stateCode = NetState.NET_UNKNOWN;
                    }
                    break;
                default:
                    stateCode = NetState.NET_UNKNOWN;
            }
        }

        return stateCode;

    }	
	public interface NetStateChangedListener{
		void onNetStateChanged(NetState netCode);
	}

}
