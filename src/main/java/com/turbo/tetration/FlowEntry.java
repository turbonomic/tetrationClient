package com.turbo.tetration;

/**
 * Created by steven on 8/17/17.
 */
public class FlowEntry {
    String dst_address;
    String dst_hostname;
    int dst_port;

    String src_address;
    String src_hostname;
    int src_port;

    long fwd_bytes;
    long rev_bytes;
    long fwd_pkts;
    long rev_pkts;

    public long getFwd_pkts() {
        return fwd_pkts;
    }

    public void setFwd_pkts(long fwd_pkts) {
        this.fwd_pkts = fwd_pkts;
    }

    public long getRev_pkts() {
        return rev_pkts;
    }

    public void setRev_pkts(long rev_pkts) {
        this.rev_pkts = rev_pkts;
    }

    long total_network_latency_usec;
    long record_start_ts_usec;
    long record_end_ts_usec;

    public String getDst_address() {
        return dst_address;
    }

    public void setDst_address(String dst_address) {
        this.dst_address = dst_address;
    }

    public String getDst_hostname() {
        return dst_hostname;
    }

    public void setDst_hostname(String dst_hostname) {
        this.dst_hostname = dst_hostname;
    }

    public int getDst_port() {
        return dst_port;
    }

    public void setDst_port(int dst_port) {
        this.dst_port = dst_port;
    }

    public String getSrc_address() {
        return src_address;
    }

    public void setSrc_address(String src_address) {
        this.src_address = src_address;
    }

    public String getSrc_hostname() {
        return src_hostname;
    }

    public void setSrc_hostname(String src_hostname) {
        this.src_hostname = src_hostname;
    }

    public int getSrc_port() {
        return src_port;
    }

    public void setSrc_port(int src_port) {
        this.src_port = src_port;
    }

    public long getFwd_bytes() {
        return fwd_bytes;
    }

    public void setFwd_bytes(long fwd_bytes) {
        this.fwd_bytes = fwd_bytes;
    }

    public long getRev_bytes() {
        return rev_bytes;
    }

    public void setRev_bytes(long rev_bytes) {
        this.rev_bytes = rev_bytes;
    }

    public long getTotal_network_latency_usec() {
        return total_network_latency_usec;
    }

    public void setTotal_network_latency_usec(long total_network_latency_usec) {
        this.total_network_latency_usec = total_network_latency_usec;
    }

    public long getRecord_start_ts_usec() {
        return record_start_ts_usec;
    }

    public void setRecord_start_ts_usec(long record_start_ts_usec) {
        this.record_start_ts_usec = record_start_ts_usec;
    }

    public long getRecord_end_ts_usec() {
        return record_end_ts_usec;
    }

    public void setRecord_end_ts_usec(long record_end_ts_usec) {
        this.record_end_ts_usec = record_end_ts_usec;
    }
}
