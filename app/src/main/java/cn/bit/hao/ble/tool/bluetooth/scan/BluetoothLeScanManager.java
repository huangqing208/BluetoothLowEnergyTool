/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.response.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/3
 */
public class BluetoothLeScanManager implements CommonResponseCallback {
    private static final String TAG = BluetoothLeScanManager.class.getSimpleName();

    private WeakReference<Context> applicationContext;

    private static BluetoothLeScanManager instance;

    private boolean isLeScanning = false;
    private boolean needRecoveryScanning = false;

    private LeScanCallbackImpl leScanCallback;
    private ScanCallbackImpl scanCallback;

    private BluetoothLeScanManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            leScanCallback = new LeScanCallbackImpl();
        } else {
            scanCallback = new ScanCallbackImpl();
        }
    }

    public static synchronized BluetoothLeScanManager getInstance() {
        if (instance == null) {
            instance = new BluetoothLeScanManager();
        }
        return instance;
    }

    /**
     * 初始化BluetoothDiscoveryManager，具体实现是获取Application上下文对象保证工作正常。
     *
     * @param context 上下文对象
     * @return 如果初始化成功则返回true，否则返回false
     */
    public boolean initManager(Context context) {
        if (context == null) {
            return false;
        }
        if (applicationContext != null && applicationContext.get() != null) {
            return false;
        }
        applicationContext = new WeakReference<Context>(context.getApplicationContext());

        CommonResponseManager.getInstance().addTaskCallback(instance);

        // 假使上次finish无能将leScanCallback停止掉，那么这里必须先停止掉，才能确保之后startLeScan无虞
        if (isLeScanning) {
            stopLeScan();
        }
        return true;
    }

    /**
     * finish方法和initManager方法成对使用，不可单一多次调用
     */
    public void finish() {
        if (isLeScanning) {
            // 必须停止搜索
            stopLeScan();
        }

        CommonResponseManager.getInstance().removeTaskCallback(this);
        applicationContext = null;
    }

    private Context getContext() {
        return (applicationContext == null || applicationContext.get() == null)
                ? null
                : applicationContext.get();
    }

    /**
     * 开启Le搜索
     *
     * @return 如果在搜索的话返回true，否则返回false
     */
    public synchronized boolean startLeScan() {
        if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
            needRecoveryScanning = true;
            return false;
        }
        Context context = getContext();
        if (context == null) {
            return false;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
        if (bluetoothAdapter == null) {
            return false;
        }
        if (isLeScanning) {
            // 在stopLeScan前不允许重复startLeScan避免触发SCAN_FAILED_ALREADY_STARTED
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.startLeScan(leScanCallback);
            isLeScanning = true;
        } else {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                bluetoothLeScanner.startScan(null, settings, scanCallback);
                // TODO: assume that perform successful if no ScanFiledEvent is occurring
                isLeScanning = true;
            }
        }
        if (isLeScanning) {
            // 无法检测是否真的start成功，所以最好做应用层超时检测
            // TODO: wait for 20s, if no result, ask for reset bluetooth
            Log.i(TAG, "start scanning.");
        }
        return isLeScanning;
    }

    /**
     * 停止Le搜索
     */
    public synchronized void stopLeScan() {
        if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
            return;
        }
        Context context = getContext();
        if (context == null) {
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
        if (bluetoothAdapter == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.stopLeScan(leScanCallback);
            isLeScanning = false;
        } else {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
                isLeScanning = false;
            }
        }
        Log.i(TAG, "stop scanning.");
    }

    @Override
    public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
        if (commonResponseEvent instanceof BluetoothStateEvent) {
            switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
                case BLUETOOTH_STATE_ON:
                    if (needRecoveryScanning) {
                        needRecoveryScanning = false;
                        // 如果此前isLeScanning为true，表示之前是在搜索中的，那么这里得重新搜索，即终止上次搜索，再开始新搜索
                        if (isLeScanning) {
                            stopLeScan();
                        }
                        startLeScan();
                    }
                    break;
                case BLUETOOTH_STATE_OFF:
                    // when bluetooth off, scan is passively stopped
                    if (isLeScanning) {
                        needRecoveryScanning = true;
                    }
                    break;
                case BLUETOOTH_STATE_ERROR:
                    break;
                default:
                    break;
            }
        }
    }
}
