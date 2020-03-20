package edu.pec.dromeas.payload;

import lombok.Data;

@Data
public class SystemStat
{
    private double maxMemory;
    private double freeMemory;
    private double totalMemory;
    private long processors;
    private double totalDisk;
    private double freeDisk;
}
