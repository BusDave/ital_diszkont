package com.example.teszt;

public class ShoppingItem {
    private  String name;
    private String info;

    private String price;
    private float rated;
    private int imageResource;

    private int cartedCount;

    private String id;

    public ShoppingItem(){};
    public ShoppingItem(int imageResource, String name, String info, String price, float rated, int cartedCount) {
        this.imageResource = imageResource;
        this.name = name;
        this.info = info;
        this.price = price;
        this.rated = rated;
        this.cartedCount = cartedCount;
    }

    public String getName() {
        return name;
    }

    public int getCartedCount() {
        return cartedCount;
    }

    public String getInfo() {
        return info;
    }

    public String getPrice() {
        return price;
    }

    public float getRated() {
        return rated;
    }

    public int getImageResource() {
        return imageResource;
    }
    public String _getId(){
        return id;
    }
    public void setId(String id1){
        this.id = id1;
    }
}
