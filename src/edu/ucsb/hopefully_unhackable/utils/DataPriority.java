package edu.ucsb.hopefully_unhackable.utils;

import java.util.Comparator;

public final class DataPriority implements Comparator<DataBlock> {
	  public static final DataPriority INSTANCE = new DataPriority();

	  private DataPriority() {}

	  @Override
	  public int compare(DataBlock block1, DataBlock block2) {
	    return Integer.valueOf(block1.getOffset()).compareTo(block2.getOffset());
	  }

	  @Override
	  public boolean equals(Object other) {
	    return other == DataPriority.INSTANCE;
	  }

	  private Object readResolve() {
	    return INSTANCE;
	  }
}