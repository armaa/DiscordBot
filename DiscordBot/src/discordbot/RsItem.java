/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discordbot;

/**
 *
 * @author armaa
 */
public class RsItem implements Comparable<RsItem> {

    private String name;
    private int price;
    private int hialch;
    private int diff;

    public RsItem(String name, int price, int hialch, int diff) {
        this.name = name;
        this.price = price;
        this.hialch = hialch;
        this.diff = diff;
    }    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the price
     */
    public int getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * @return the hialch
     */
    public int getHialch() {
        return hialch;
    }

    /**
     * @param hialch the hialch to set
     */
    public void setHialch(int hialch) {
        this.hialch = hialch;
    }

    /**
     * @return the diff
     */
    public int getDiff() {
        return diff;
    }

    /**
     * @param diff the diff to set
     */
    public void setDiff(int diff) {
        this.diff = diff;
    }

    @Override
    public int compareTo(RsItem other) {
        return Integer.compare(other.diff, this.diff);
    }
}
