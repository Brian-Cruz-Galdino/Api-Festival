package org.acme;

public class PageRequest {
    public int page = 0;
    public int size = 10;
    public String sort = "id";
    public String direction = "asc";

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}