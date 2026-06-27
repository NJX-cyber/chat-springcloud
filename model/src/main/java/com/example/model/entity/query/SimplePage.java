package com.example.model.entity.query;


import com.example.model.enums.PageSize;

public class SimplePage {
    private Integer pageNum;
    private Integer countTotal;
    private Integer pageSize;
    private Integer pageTotal;
    private Integer start;
    private Integer end;

    public SimplePage() {

    }

    public SimplePage(Integer pageNum, Integer countTotal, Integer pageSize) {
        if (pageNum == null) {
            pageNum = 1;
        }
        this.pageNum = pageNum;
        this.countTotal = countTotal;
        this.pageSize = pageSize;
        action();
    }

    public SimplePage(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    private void action() {
        if (this.pageSize <= 0) {
            this.pageSize = PageSize.SIZE20.getSize();
        }
        if (this.countTotal > 0) {
            this.pageTotal = this.countTotal % this.pageSize == 0 ? (this.countTotal / this.pageSize)
                    : (this.countTotal / this.pageSize + 1);
        } else {
            pageTotal = 1;
        }
        if (pageNum > pageTotal) {
            pageNum = pageTotal;
        }
        this.start = (pageNum - 1) * pageSize;
        this.end = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(Integer countTotal) {
        this.countTotal = countTotal;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
