package br.facens.beacon;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fgoncalves on 12/11/2017.
 */

public class MeuBeaconConsumer implements BeaconConsumer, RangeNotifier {

    private static final String FAROL_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private MainActivity activity;
    private BeaconManager beaconManager;

    private Map<String, Date> beaconMap;

    public MeuBeaconConsumer(MainActivity activity) {
        this.activity = activity;
    }

    public void iniciarExecucao() {
        this.beaconMap = new HashMap<>();
        beaconManager = BeaconManager.getInstanceForApplication(activity);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(FAROL_LAYOUT));
        beaconManager.addRangeNotifier(this);
        beaconManager.bind(this);
    }

    public void pararExecucao() {
        beaconManager.unbind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        for (Beacon b : collection) {
            beaconMap.put(b.getId1().toString(), new Date());
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("qualquerBeacon", null, null, null));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Context getApplicationContext() {
        return activity.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        activity.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return activity.bindService(intent, serviceConnection, i);
    }

    public Map<String, Date> getBeaconMap() {
        return beaconMap;
    }
}
