package com.jalvaviel.addon.utils;

import java.util.ArrayList;

public class MapBackground {
    int width;
    int height;
    Map map = null;
    ArrayList<ArrayList<Map>> mapMatrix = null;

    public MapBackground(Map map){
        this.width = map.width+14;
        this.height = map.height+14;
        this.map = map;
    }

    public MapBackground(ArrayList<ArrayList<Map>> mapMatrix){
        this.width = mapMatrix.size()+14;
        this.height = mapMatrix.isEmpty() ? 142 : mapMatrix.get(0).size()+14;
    }
}
