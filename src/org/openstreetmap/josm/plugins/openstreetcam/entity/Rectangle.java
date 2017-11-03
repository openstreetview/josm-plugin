package org.openstreetmap.josm.plugins.openstreetcam.entity;


/**
 * Defines the rectangle business entity; represents the detection position in a photo.
 *
 * @author ioanao
 * @version $Revision$
 */
public class Rectangle {

    private final Double x;
    private final Double y;
    private final Double width;
    private final Double height;


    public Rectangle(final Double x, final Double y, final Double width, final Double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }
}