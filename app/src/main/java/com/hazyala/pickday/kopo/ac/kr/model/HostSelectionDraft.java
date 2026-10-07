package com.hazyala.pickday.kopo.ac.kr.model;

import java.util.ArrayList;
import java.util.List;

public final class HostSelectionDraft {
    public final List<String> dates;
    public final List<String> times;
    public final List<String> excluded;
    public HostSelectionDraft(List<String> dates, List<String> times, List<String> excluded) {
        this.dates = new ArrayList<>(dates);
        this.times = new ArrayList<>(times);
        this.excluded = new ArrayList<>(excluded);
    }
}
