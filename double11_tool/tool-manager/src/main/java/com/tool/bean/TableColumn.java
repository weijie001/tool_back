package com.tool.bean;

import java.util.ArrayList;
import java.util.List;

public class TableColumn {

    private List<TableCell> cells = new ArrayList<>();

    public List<TableCell> getCells() {
        return cells;
    }

    public void setCells(List<TableCell> cells) {
        this.cells = cells;
    }
}
