package com.teamltt.carcare.adapter.simulator;

import android.os.Debug;
import android.util.Log;

import com.teamltt.carcare.adapter.IObdSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Jack on 1/25/2017.
 */

public class SimulatedSocket implements IObdSocket {

    private boolean connected = false;
    CountDownLatch latch;
    private byte[] buffer;
    private int numRead;

    public SimulatedSocket() {
        connected = true;
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() throws IOException {
        // Noop, because we never really open a socket
        return;
    }

    @Override
    public void writeTo(byte[] bytes) throws IOException {
        String request = new String(bytes);
        if(request.equals("ATZ\r")) {
            // todo null check on buffer
            byte[] testResponse = "read".getBytes();
            for(int i = 0; i < 4; i++) {
                buffer[i] = testResponse[i];
            }
            Log.i("debug Sim", "sending request to sim");
            latch.countDown();
        } else {
            // nothing
        }
    }

    @Override
    public int readFrom(byte[] buffer) throws IOException {
        this.buffer = buffer;
        latch = new CountDownLatch(1);
        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return numRead;

    }
}
