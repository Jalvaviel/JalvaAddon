package com.jalvaviel.addon.utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.UUID;

public interface Canvas {
    int width = 0;
    int height = 0;
    boolean mapBg = false;
    ArrayList<ArrayList<Map>> mapMatrix = new ArrayList<>();
    UUID canvasID = null;
    BufferedImage bufferedCanvas = null;

    

}
