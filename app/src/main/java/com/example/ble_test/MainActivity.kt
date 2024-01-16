package com.example.ble_test


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ble_test.ui.theme.BLE_TESTTheme
import android.widget.ListView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build
import androidx.core.content.ContextCompat



// Stops scanning after 10 seconds.

class MainActivity : ComponentActivity() {

    lateinit var listView: ListView
    var arrayList: ArrayList<MyData> = ArrayList()
    var adapter: MyAdapter? = null
    private val SCAN_PERIOD: Long = 10000 // 10 seconds

    private val handler = Handler()
    private val BLUETOOTH_PERMISSION_REQUEST = 1
    private val ENABLE_BLUETOOTH_REQUEST = 2
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    var counter=0

    private val bleScanCallback = object : android.bluetooth.le.ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
            result?.device?.let {
                // Handle the discovered BLE device here
                // For example, update a list or perform other actions
                val searchTerm = it.address.toString()
                val isPresent = arrayList.any { it.mobileNumber == searchTerm }
                if(!isPresent){
                    counter++
                    if(it.name==null)
                    {
                        arrayList.add(MyData(counter , "?", it.address.toString()))
                    }else{
                        arrayList.add(MyData(counter , it.name.toString(), it.address.toString()))
                    }


                    listView.adapter = adapter
                }


                println("Found BLE device: ${it.name} - ${it.address}")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.out.println("Hello World!");
        print("Hello kotlin")
        print("asdsad")
        title = "KotlinApp"
        listView = findViewById(R.id.listView)
        //arrayList.add(MyData(1, " Mashu", "987576443"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestBluetoothPermission()
        } else {
            initializeBluetoothAdapter()
            startBleScan()
        }

        //arrayList.add(MyData(2, " Azhar", "8787576768"))
        //arrayList.add(MyData(3, " Niyaz", "65757657657"))
        adapter = MyAdapter(this, arrayList)
        listView.adapter = adapter
    }
    private fun requestBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_FINE_LOCATION),
                BLUETOOTH_PERMISSION_REQUEST
            )
        } else {
            initializeBluetoothAdapter()
            startBleScan()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeBluetooth()
                startBleScan()
            } else {
                // Handle the case when Bluetooth permission is not granted
                // For example, display a message to the user
            }
        }
    }
    private fun initializeBluetooth() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }
    private fun initializeBluetoothAdapter() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    private fun startBleScan() {
        if (bluetoothAdapter == null || bluetoothLeScanner == null || !bluetoothAdapter!!.isEnabled) {
            // Bluetooth not supported or not enabled
            return
        }

        val scanner = bluetoothAdapter!!.bluetoothLeScanner
        try {
            //scanner?.startScan(bleScanCallback)

            bluetoothLeScanner?.startScan(bleScanCallback)
            handler.postDelayed({
                stopBleScan()
            }, SCAN_PERIOD)
        }catch (e:Exception){
            println("ble driver :")
            println(e.toString())

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
    }

    private fun stopBleScan() {
        if (bluetoothLeScanner != null && bluetoothAdapter != null && bluetoothAdapter!!.isEnabled) {
            bluetoothLeScanner?.stopScan(bleScanCallback)
        }
    }

    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST = 1
    }
}
//Class MyAdapter
class MyAdapter(private val context: Context, private val arrayList: java.util.ArrayList<MyData>) : BaseAdapter() {
    private lateinit var serialNum: TextView
    private lateinit var name: TextView
    private lateinit var contactNum: TextView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.custom_list, parent, false)
        if (convertView != null) {
            serialNum = convertView.findViewById(R.id.serialNumber)
            name = convertView.findViewById(R.id.studentName)
            contactNum = convertView.findViewById(R.id.mobileNum)
        }

        serialNum.text = " " + arrayList[position].num
        name.text = arrayList[position].name
        contactNum.text = arrayList[position].mobileNumber
        return convertView
    }
}

//class MyData
class MyData(var num: Int, var name: String, var mobileNumber: String)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BLE_TESTTheme {
        Greeting("Android")
    }
}