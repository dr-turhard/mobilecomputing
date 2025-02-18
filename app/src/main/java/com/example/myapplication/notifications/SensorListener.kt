package com.example.myapplication.notifications

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.myapplication.MainActivity
import com.example.myapplication.notifications.Notifications
import com.example.myapplication.notifications.Notifications.triggerNotification

class SensorListener(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    init{
        accelerometer?.let{
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let{ values -> val acceleration = Math.sqrt(
            (values[0] * values[0] + values[1] + values[2] * values[2]).toDouble()
        )

        if(acceleration > 10){ //Should trigger always when accelerated (?)
           triggerNotification(context, acceleration)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //TODO("Not yet implemented")
    }
}