package skipnode;

import java.io.Serializable;
import java.util.List;

/** Represents the result of a search containing the piggybacked information. */
public class SearchResult implements Serializable {

  // todo: this should be renamed to identity and also should be private.
  public final SkipNodeIdentity result;

  public SearchResult(SkipNodeIdentity result) {
    this.result = result;
  }
}
