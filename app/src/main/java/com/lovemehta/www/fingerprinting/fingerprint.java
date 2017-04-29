package com.lovemehta.www.fingerprinting;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mayank on 4/20/17.
 */

public class fingerprint implements Serializable{
    public String location;
    public ArrayList<String> ssidList, bssidList, cap, freq, level, timeStamp;
}
