package de.rwth.processmining.tb.core.sps.algorithm.dto;

import java.util.Arrays;

public record DiffCandidateShortInfo(int id, int idCondActUnion, String[] activities, String[] conditionActivities, double metric) {

  @Override
  public String toString() {
    return "DiffCandidateShortInfo [id=" + id + ", idCondActUnion=" + idCondActUnion + ", activities="
        + Arrays.toString(activities) + ", conditionActivities=" + Arrays.toString(conditionActivities) + ", metric="
        + metric + "]";
  }

}
